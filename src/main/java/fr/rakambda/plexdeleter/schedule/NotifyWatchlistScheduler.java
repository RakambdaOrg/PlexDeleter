package fr.rakambda.plexdeleter.schedule;

import fr.rakambda.plexdeleter.config.ApplicationConfiguration;
import fr.rakambda.plexdeleter.notify.NotificationService;
import fr.rakambda.plexdeleter.notify.NotifyException;
import fr.rakambda.plexdeleter.storage.entity.MediaRequirementStatus;
import fr.rakambda.plexdeleter.storage.entity.UserGroupEntity;
import fr.rakambda.plexdeleter.storage.repository.MediaRequirementRepository;
import fr.rakambda.plexdeleter.storage.repository.UserGroupRepository;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.time.ZonedDateTime;

@Slf4j
@Component
public class NotifyWatchlistScheduler implements IScheduler{
	private final MediaRequirementRepository mediaRequirementRepository;
	private final UserGroupRepository userGroupRepository;
	private final NotificationService notificationService;
	private final int daysDelay;
	
	@Autowired
	public NotifyWatchlistScheduler(MediaRequirementRepository mediaRequirementRepository, UserGroupRepository userGroupRepository, NotificationService notificationService, ApplicationConfiguration applicationConfiguration){
		this.mediaRequirementRepository = mediaRequirementRepository;
		this.userGroupRepository = userGroupRepository;
		this.notificationService = notificationService;
		this.daysDelay = applicationConfiguration.getNotifyWatchlist().getDaysDelay();
	}
	
	@Override
	@NonNull
	public String getTaskId(){
		return "notify-watchlist";
	}
	
	@Override
	@Scheduled(cron = "0 30 1 * * *")
	@Transactional
	public void run(){
		log.info("Notifying user groups");
		var before = ZonedDateTime.now().minusDays(daysDelay).toInstant();
		var userGroups = userGroupRepository.findAllByLastNotificationBefore(before);
		for(var userGroup : userGroups){
			try{
				update(userGroup);
			}
			catch(Exception e){
				log.error("Failed to notify user group {}", userGroup, e);
			}
		}
		
		log.info("Done notifying {} user groups", userGroups.size());
	}
	
	void update(@NonNull UserGroupEntity userGroupEntity) throws NotifyException{
		log.info("Notifying user group {}", userGroupEntity);
		
		var requirements = mediaRequirementRepository.findAllByIdGroupIdAndStatusIs(userGroupEntity.getId(), MediaRequirementStatus.WAITING);
		notificationService.notifyWatchlist(userGroupEntity, requirements);
		
		userGroupEntity.setLastNotification(Instant.now());
		userGroupRepository.save(userGroupEntity);
	}
}
