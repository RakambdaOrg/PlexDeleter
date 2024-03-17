package fr.rakambda.plexdeleter.service;

import fr.rakambda.plexdeleter.api.RequestFailedException;
import fr.rakambda.plexdeleter.messaging.SupervisionService;
import fr.rakambda.plexdeleter.notify.NotificationService;
import fr.rakambda.plexdeleter.notify.NotifyException;
import fr.rakambda.plexdeleter.storage.entity.MediaEntity;
import fr.rakambda.plexdeleter.storage.entity.MediaRequirementEntity;
import fr.rakambda.plexdeleter.storage.entity.MediaRequirementStatus;
import fr.rakambda.plexdeleter.storage.entity.UserGroupEntity;
import fr.rakambda.plexdeleter.storage.repository.MediaRequirementRepository;
import fr.rakambda.plexdeleter.storage.repository.UserGroupRepository;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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
	
	@Autowired
	public MediaRequirementService(MediaRequirementRepository mediaRequirementRepository, NotificationService notificationService, SupervisionService supervisionService, MediaService mediaService, UserGroupRepository userGroupRepository){
		this.mediaRequirementRepository = mediaRequirementRepository;
		this.notificationService = notificationService;
		this.supervisionService = supervisionService;
		this.mediaService = mediaService;
		this.userGroupRepository = userGroupRepository;
		this.requirementOperationLock = new ReentrantLock();
	}
	
	public void complete(@NotNull MediaRequirementEntity requirement) throws NotifyException, ServiceException{
		requirementOperationLock.lock();
		try{
			if(!requirement.getMedia().isCompletable()){
				throw new ServiceException("Cannot complete a media that isn't fully available");
			}
			log.info("Marking requirement {} as completed", requirement);
			
			requirement.setStatus(MediaRequirementStatus.WATCHED);
			
			var group = requirement.getGroup();
			var media = requirement.getMedia();
			
			notificationService.notifyRequirementManuallyWatched(group, media);
			mediaRequirementRepository.save(requirement);
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
			mediaRequirementRepository.save(requirement);
			
			mediaService.deleteMedia(media, true);
			
			notificationService.notifyRequirementManuallyAbandoned(group, media);
			supervisionService.send("✍\uFE0F\uD83D\uDE48 Media manually abandoned %s for %s", media, group);
		}
		finally{
			requirementOperationLock.unlock();
		}
	}
	
	public void addRequirement(@NotNull MediaEntity media, @NotNull UserGroupEntity userGroupEntity, boolean allowModify) throws NotifyException{
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
						.build());
				supervisionService.send("\uD83D\uDED2 Added requirement %s to %s", media, userGroupEntity);
				notificationService.notifyRequirementAdded(userGroupEntity, media);
				return;
			}
			
			if(!allowModify){
				log.info("Requirement already exists but we're not modifying it");
				return;
			}
			
			log.info("Updating already existing requirement");
			mediaRequirementRepository.findById(id)
					.map(requirement -> {
						requirement.setStatus(MediaRequirementStatus.WAITING);
						return requirement;
					})
					.ifPresent(mediaRequirementRepository::save);
			supervisionService.send("Updated requirement %s to %s", media, userGroupEntity);
			notificationService.notifyRequirementAdded(userGroupEntity, media);
		}
		finally{
			requirementOperationLock.unlock();
		}
	}
	
	public void addRequirementForNewMedia(@NotNull MediaEntity media, @NotNull UserGroupEntity userGroupEntity) throws NotifyException{
		log.info("Adding requirements to media {}", media);
		addRequirement(media, userGroupEntity, true);
		
		var otherGroups = userGroupRepository.findAllByHasRequirementOn(Objects.requireNonNull(media.getOverseerrId()), media.getIndex() - 1);
		for(var otherGroup : otherGroups){
			if(Objects.equals(otherGroup.getId(), userGroupEntity.getId())){
				continue;
			}
			addRequirement(media, otherGroup, false);
		}
	}
}
