package fr.rakambda.plexdeleter.schedule;

import fr.rakambda.plexdeleter.api.overseerr.OverseerrService;
import fr.rakambda.plexdeleter.api.radarr.RadarrService;
import fr.rakambda.plexdeleter.api.sonarr.SonarrService;
import fr.rakambda.plexdeleter.api.tautulli.TautulliService;
import fr.rakambda.plexdeleter.config.ApplicationConfiguration;
import fr.rakambda.plexdeleter.messaging.SupervisionService;
import fr.rakambda.plexdeleter.notify.NotificationService;
import fr.rakambda.plexdeleter.notify.NotifyException;
import fr.rakambda.plexdeleter.storage.entity.MediaEntity;
import fr.rakambda.plexdeleter.storage.entity.MediaRequirementEntity;
import fr.rakambda.plexdeleter.storage.entity.MediaRequirementStatus;
import fr.rakambda.plexdeleter.storage.entity.UserGroupEntity;
import fr.rakambda.plexdeleter.storage.repository.MediaRepository;
import fr.rakambda.plexdeleter.storage.repository.MediaRequirementRepository;
import fr.rakambda.plexdeleter.storage.repository.UserGroupRepository;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.function.Predicate;

@Slf4j
@Component
public class NotifyWatchlistScheduler implements IScheduler{
	private static final Predicate<MediaEntity> hasPartsPredicate = m -> m.getPartsCount() > 0;
	private static final Predicate<MediaEntity> isFullyDownloadedPredicate = m -> m.getAvailablePartsCount() >= m.getPartsCount();
	
	private final TautulliService tautulliService;
	private final SupervisionService supervisionService;
	private final MediaRequirementRepository mediaRequirementRepository;
	private final UserGroupRepository userGroupRepository;
	private final NotificationService notificationService;
	private final int daysDelay;
	
	@Autowired
	public NotifyWatchlistScheduler(MediaRepository mediaRepository, OverseerrService overseerrService, TautulliService tautulliService, SonarrService sonarrService, RadarrService radarrService, SupervisionService supervisionService, MediaRequirementRepository mediaRequirementRepository, UserGroupRepository userGroupRepository, NotificationService notificationService, ApplicationConfiguration applicationConfiguration){
		this.tautulliService = tautulliService;
		this.supervisionService = supervisionService;
		this.mediaRequirementRepository = mediaRequirementRepository;
		this.userGroupRepository = userGroupRepository;
		this.notificationService = notificationService;
		this.daysDelay = applicationConfiguration.getNotifyWatchlist().getDaysDelay();
	}
	
	@Override
	@NotNull
	public String getTaskId(){
		return "notify-watchlist";
	}
	
	@Override
	@Scheduled(cron = "0 30 1 * * *")
	@Transactional
	public void run(){
		var before = ZonedDateTime.now().minusDays(daysDelay).toInstant();
		var userGroups = userGroupRepository.findAllByLastNotificationBefore(before);
		for(var userGroup : userGroups){
			try{
				update(userGroup);
			}
			catch(NotifyException e){
				log.error("Failed to notify user group {}", userGroup, e);
			}
		}
		
		log.info("Done notifying {} user groups", userGroups.size());
	}
	
	@VisibleForTesting
	void update(@NotNull UserGroupEntity userGroupEntity) throws NotifyException{
		log.info("Notifying user group {}", userGroupEntity);
		
		var requirements = mediaRequirementRepository.findAllByIdGroupIdAndStatusIs(userGroupEntity.getId(), MediaRequirementStatus.WAITING);
		
		var fullyAvailable = requirements.stream()
				.map(MediaRequirementEntity::getMedia)
				.filter(isFullyDownloadedPredicate.and(isFullyDownloadedPredicate))
				.filter(m -> m.getAvailablePartsCount() >= m.getPartsCount())
				.toList();
		var notYetAvailable = requirements.stream()
				.map(MediaRequirementEntity::getMedia)
				.filter(isFullyDownloadedPredicate.negate().or(isFullyDownloadedPredicate.negate()))
				.toList();
		
		notificationService.notifyWatchlist(userGroupEntity, fullyAvailable, notYetAvailable);
		
		userGroupEntity.setLastNotification(Instant.now());
		userGroupRepository.save(userGroupEntity);
	}
}
