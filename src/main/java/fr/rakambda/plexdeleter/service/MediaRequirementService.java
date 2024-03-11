package fr.rakambda.plexdeleter.service;

import fr.rakambda.plexdeleter.messaging.SupervisionService;
import fr.rakambda.plexdeleter.notify.NotificationService;
import fr.rakambda.plexdeleter.notify.NotifyException;
import fr.rakambda.plexdeleter.storage.entity.MediaEntity;
import fr.rakambda.plexdeleter.storage.entity.MediaRequirementEntity;
import fr.rakambda.plexdeleter.storage.entity.MediaRequirementStatus;
import fr.rakambda.plexdeleter.storage.entity.UserGroupEntity;
import fr.rakambda.plexdeleter.storage.repository.MediaRequirementRepository;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MediaRequirementService{
	private final MediaRequirementRepository mediaRequirementRepository;
	private final NotificationService notificationService;
	private final SupervisionService supervisionService;
	
	@Autowired
	public MediaRequirementService(MediaRequirementRepository mediaRequirementRepository, NotificationService notificationService, SupervisionService supervisionService){
		this.mediaRequirementRepository = mediaRequirementRepository;
		this.notificationService = notificationService;
		this.supervisionService = supervisionService;
	}
	
	public void complete(int mediaId, int groupId) throws NotifyException{
		var requirement = mediaRequirementRepository.findById(new MediaRequirementEntity.TableId(mediaId, groupId));
		if(requirement.isEmpty()){
			return;
		}
		log.info("Marking requirement {} as completed", requirement);
		
		requirement.get().setStatus(MediaRequirementStatus.WATCHED);
		
		var group = requirement.get().getGroup();
		var media = requirement.get().getMedia();
		
		notificationService.notifyRequirementManuallyWatched(group, media);
		mediaRequirementRepository.save(requirement.get());
		supervisionService.send("✍\uFE0F\uD83D\uDC41\uFE0F Media manually watched %s for %s", media, group);
	}
	
	public void abandon(int mediaId, int groupId) throws NotifyException{
		var requirement = mediaRequirementRepository.findById(new MediaRequirementEntity.TableId(mediaId, groupId));
		if(requirement.isEmpty()){
			return;
		}
		log.info("Marking requirement {} as abandoned", requirement);
		
		requirement.get().setStatus(MediaRequirementStatus.ABANDONED);
		
		var group = requirement.get().getGroup();
		var media = requirement.get().getMedia();
		
		notificationService.notifyRequirementManuallyAbandoned(group, media);
		mediaRequirementRepository.save(requirement.get());
		supervisionService.send("✍\uFE0F\uD83D\uDE48 Media manually abandoned %s for %s", media, group);
	}
	
	public void addRequirement(@NotNull MediaEntity media, @NotNull UserGroupEntity userGroupEntity, boolean allowModify) throws NotifyException{
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
}
