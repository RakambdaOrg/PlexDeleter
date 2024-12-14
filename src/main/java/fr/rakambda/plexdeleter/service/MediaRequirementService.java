package fr.rakambda.plexdeleter.service;

import fr.rakambda.plexdeleter.api.RequestFailedException;
import fr.rakambda.plexdeleter.api.servarr.radarr.RadarrService;
import fr.rakambda.plexdeleter.api.servarr.sonarr.SonarrService;
import fr.rakambda.plexdeleter.messaging.SupervisionService;
import fr.rakambda.plexdeleter.notify.NotificationService;
import fr.rakambda.plexdeleter.notify.NotifyException;
import fr.rakambda.plexdeleter.storage.entity.MediaEntity;
import fr.rakambda.plexdeleter.storage.entity.MediaRequirementEntity;
import fr.rakambda.plexdeleter.storage.entity.MediaRequirementStatus;
import fr.rakambda.plexdeleter.storage.entity.UserGroupEntity;
import fr.rakambda.plexdeleter.storage.repository.MediaRequirementRepository;
import fr.rakambda.plexdeleter.storage.repository.UserGroupRepository;
import fr.rakambda.plexdeleter.web.api.ThymeleafMessageException;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Service
public class MediaRequirementService{
	private final MediaRequirementRepository mediaRequirementRepository;
	private final NotificationService notificationService;
	private final SupervisionService supervisionService;
	private final MediaService mediaService;
	private final UserGroupRepository userGroupRepository;
	private final Lock requirementOperationLock;
	private final RadarrService radarrService;
	private final SonarrService sonarrService;
	
	@Autowired
	public MediaRequirementService(MediaRequirementRepository mediaRequirementRepository, NotificationService notificationService, SupervisionService supervisionService, MediaService mediaService, UserGroupRepository userGroupRepository, RadarrService radarrService, SonarrService sonarrService){
		this.mediaRequirementRepository = mediaRequirementRepository;
		this.notificationService = notificationService;
		this.supervisionService = supervisionService;
		this.mediaService = mediaService;
		this.userGroupRepository = userGroupRepository;
		this.requirementOperationLock = new ReentrantLock();
		this.radarrService = radarrService;
		this.sonarrService = sonarrService;
	}
	
	public void complete(@NotNull MediaRequirementEntity requirement) throws NotifyException, ServiceException{
		requirementOperationLock.lock();
		try{
			if(!requirement.getMedia().isCompletable()){
				throw new ServiceException("Cannot complete a media that isn't fully available");
			}
			log.info("Marking requirement {} as completed", requirement);
			
			requirement.setStatus(MediaRequirementStatus.WATCHED);
			requirement.setLastCompletedTime(Instant.now());
			mediaRequirementRepository.save(requirement);
			
			var group = requirement.getGroup();
			var media = requirement.getMedia();
			
			notificationService.notifyRequirementManuallyWatched(group, media);
			supervisionService.send("✍\uFE0F\uD83D\uDC41\uFE0F Media manually watched %s for %s", media, group);
		}
		finally{
			requirementOperationLock.unlock();
		}
	}
	
	public void abandon(@NotNull MediaRequirementEntity requirement) throws NotifyException, RequestFailedException{
		requirementOperationLock.lock();
		try{
			log.info("Marking requirement {} as abandoned", requirement);
			
			var group = requirement.getGroup();
			var media = requirement.getMedia();
			
			requirement.setStatus(MediaRequirementStatus.ABANDONED);
			requirement.setLastCompletedTime(Instant.now());
			mediaRequirementRepository.save(requirement);
			
			var deletionResult = mediaService.deleteMedia(media, requirement.getGroup(), true);
			if(!deletionResult.deletedServarr()){
				removeServarrTag(media, group);
			}
			
			notificationService.notifyRequirementManuallyAbandoned(group, media);
			supervisionService.send("✍\uFE0F\uD83D\uDE48 Media manually abandoned %s for %s", media, group);
		}
		finally{
			requirementOperationLock.unlock();
		}
	}
	
	public void addRequirementForNewMedia(@NotNull MediaEntity media, @Nullable UserGroupEntity userGroupEntity) throws NotifyException, RequestFailedException, UpdateException, ThymeleafMessageException{
		log.info("Adding requirements to media {}", media);
		
		var status = media.getStatus();
		if(!status.isOnDiskOrWillBe()){
			throw new ThymeleafMessageException("Failed to add requirement, media is not available anymore", "#{requirement.add.unavailable}");
		}
		
		var added = false;
		if(Objects.nonNull(userGroupEntity)){
			added |= addRequirement(media, userGroupEntity, true);
		}
		
		var otherGroups = userGroupRepository.findAllByHasRequirementOn(Objects.requireNonNull(media.getOverseerrId()), media.getIndex() - 1);
		for(var otherGroup : otherGroups){
			if(Objects.nonNull(userGroupEntity) && Objects.equals(otherGroup.getId(), userGroupEntity.getId())){
				continue;
			}
			added |= addRequirement(media, otherGroup, false);
		}
		
		if(added){
			mediaService.revertDeleteStatus(media.getId());
		}
	}
	
	private boolean addRequirement(@NotNull MediaEntity media, @NotNull UserGroupEntity userGroupEntity, boolean allowModify) throws NotifyException, RequestFailedException{
		requirementOperationLock.lock();
		try{
			log.info("Adding requirement on {} for {}", media, userGroupEntity);
			var id = new MediaRequirementEntity.TableId(media.getId(), userGroupEntity.getId());
			if(!mediaRequirementRepository.existsById(id)){
				mediaRequirementRepository.save(MediaRequirementEntity.builder()
						.status(MediaRequirementStatus.WAITING)
						.id(new MediaRequirementEntity.TableId(media.getId(), userGroupEntity.getId()))
						.group(userGroupEntity)
						.media(media)
						.watchedCount(0L)
						.build());
				addServarrTag(media, userGroupEntity);
				supervisionService.send("\uD83D\uDED2 Added requirement %s to %s", media, userGroupEntity);
				notificationService.notifyRequirementAdded(userGroupEntity, media);
				return true;
			}
			
			if(!allowModify){
				log.info("Requirement already exists but we're not modifying it");
				return false;
			}
			
			log.info("Updating already existing requirement");
			mediaRequirementRepository.findById(id)
					.filter(r -> !Objects.equals(r.getStatus(), MediaRequirementStatus.WAITING))
					.map(requirement -> {
						requirement.setStatus(MediaRequirementStatus.WAITING);
						requirement.setWatchedCount(0L);
						return requirement;
					})
					.ifPresent(mediaRequirementRepository::save);
			addServarrTag(media, userGroupEntity);
			supervisionService.send("Updated requirement %s to %s", media, userGroupEntity);
			notificationService.notifyRequirementAdded(userGroupEntity, media);
			return true;
		}
		finally{
			requirementOperationLock.unlock();
		}
	}
	
	private void addServarrTag(@NotNull MediaEntity media, @NotNull UserGroupEntity userGroup) throws RequestFailedException{
		try{
			if(Objects.nonNull(media.getServarrId()) && Objects.nonNull(userGroup.getServarrTag())){
				switch(media.getType()){
					case MOVIE -> radarrService.addTag(media.getServarrId(), userGroup.getServarrTag());
					case SEASON -> sonarrService.addTag(media.getServarrId(), userGroup.getServarrTag());
				}
			}
		}
		catch(Exception e){
			log.error("Failed to update tags", e);
		}
	}
	
	private void removeServarrTag(@NotNull MediaEntity media, @NotNull UserGroupEntity userGroup) throws RequestFailedException{
		try{
			if(Objects.nonNull(media.getServarrId()) && Objects.nonNull(userGroup.getServarrTag())){
				switch(media.getType()){
					case MOVIE -> radarrService.removeTag(media.getServarrId(), userGroup.getServarrTag());
					case SEASON -> sonarrService.removeTag(media.getServarrId(), userGroup.getServarrTag());
				}
			}
		}
		catch(Exception e){
			log.error("Failed to update tags", e);
		}
	}
}
