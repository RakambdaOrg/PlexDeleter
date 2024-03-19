package fr.rakambda.plexdeleter.notify;

import fr.rakambda.plexdeleter.api.RequestFailedException;
import fr.rakambda.plexdeleter.api.tautulli.data.GetMetadataResponse;
import fr.rakambda.plexdeleter.storage.entity.MediaEntity;
import fr.rakambda.plexdeleter.storage.entity.MediaRequirementEntity;
import fr.rakambda.plexdeleter.storage.entity.MediaRequirementStatus;
import fr.rakambda.plexdeleter.storage.entity.UserGroupEntity;
import fr.rakambda.plexdeleter.storage.repository.UserGroupRepository;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Objects;

@Slf4j
@Service
public class NotificationService{
	private final MailNotificationService mailNotificationService;
	private final DiscordNotificationService discordNotificationService;
	private final UserGroupRepository userGroupRepository;
	
	@Autowired
	public NotificationService(MailNotificationService mailNotificationService, DiscordNotificationService discordNotificationService, UserGroupRepository userGroupRepository){
		this.mailNotificationService = mailNotificationService;
		this.discordNotificationService = discordNotificationService;
		this.userGroupRepository = userGroupRepository;
	}
	
	public void notifyWatchlist(@NotNull UserGroupEntity userGroupEntity, @NotNull Collection<MediaRequirementEntity> requirements) throws NotifyException{
		try{
			if(requirements.isEmpty()){
				return;
			}
			log.info("Notifying watchlist to {}", userGroupEntity);
			var notification = userGroupEntity.getNotification();
			if(Objects.isNull(notification)){
				return;
			}
			switch(notification.getType()){
				case MAIL -> mailNotificationService.notifyWatchlist(notification, userGroupEntity, requirements);
				case DISCORD, DISCORD_THREAD -> discordNotificationService.notifyWatchlist(notification, userGroupEntity, requirements);
			}
		}
		catch(MessagingException | UnsupportedEncodingException | InterruptedException | RequestFailedException e){
			throw new NotifyException("Failed to notify watchlist for group %s".formatted(userGroupEntity), e);
		}
	}
	
	public void notifyRequirementAdded(@NotNull UserGroupEntity userGroupEntity, @NotNull MediaEntity media) throws NotifyException{
		try{
			log.info("Notifying a requirement on {} has been added to {}", media, userGroupEntity);
			var notification = userGroupEntity.getNotification();
			if(Objects.isNull(notification)){
				return;
			}
			switch(notification.getType()){
				case MAIL -> mailNotificationService.notifyRequirementAdded(notification, userGroupEntity, media);
				case DISCORD, DISCORD_THREAD -> discordNotificationService.notifyRequirementAdded(notification, userGroupEntity, media);
			}
		}
		catch(MessagingException | UnsupportedEncodingException | InterruptedException | RequestFailedException e){
			throw new NotifyException("Failed to notify media requirement added for group %s".formatted(userGroupEntity), e);
		}
	}
	
	@Transactional
	public void notifyMediaAvailable(@NotNull MediaEntity media) throws NotifyException{
		var userGroups = userGroupRepository.findAllByHasRequirementOnOverseerr(media.getId(), MediaRequirementStatus.WAITING);
		for(var userGroup : userGroups){
			notifyMediaAvailable(userGroup, media);
		}
	}
	
	public void notifyMediaAvailable(@NotNull UserGroupEntity userGroupEntity, @NotNull MediaEntity media) throws NotifyException{
		try{
			log.info("Notifying {} is available to {}", media, userGroupEntity);
			var notification = userGroupEntity.getNotification();
			if(Objects.isNull(notification)){
				return;
			}
			switch(notification.getType()){
				case MAIL -> mailNotificationService.notifyMediaAvailable(notification, userGroupEntity, media);
				case DISCORD, DISCORD_THREAD -> discordNotificationService.notifyMediaAvailable(notification, userGroupEntity, media);
			}
		}
		catch(MessagingException | UnsupportedEncodingException | InterruptedException | RequestFailedException e){
			throw new NotifyException("Failed to notify media added for group %s".formatted(userGroupEntity), e);
		}
	}
	
	public void notifyMediaDeleted(@NotNull UserGroupEntity userGroupEntity, @NotNull MediaEntity media) throws NotifyException{
		try{
			log.info("Notifying {} has been deleted to {}", media, userGroupEntity);
			var notification = userGroupEntity.getNotification();
			if(Objects.isNull(notification)){
				return;
			}
			switch(notification.getType()){
				case MAIL -> mailNotificationService.notifyMediaDeleted(notification, userGroupEntity, media);
				case DISCORD, DISCORD_THREAD -> discordNotificationService.notifyMediaDeleted(notification, userGroupEntity, media);
			}
		}
		catch(MessagingException | UnsupportedEncodingException | InterruptedException | RequestFailedException e){
			throw new NotifyException("Failed to notify media deleted for group %s".formatted(userGroupEntity), e);
		}
	}
	
	public void notifyRequirementManuallyWatched(@NotNull UserGroupEntity userGroupEntity, @NotNull MediaEntity media) throws NotifyException{
		try{
			log.info("Notifying {} has been manually marked as watched to {}", media, userGroupEntity);
			var notification = userGroupEntity.getNotification();
			if(Objects.isNull(notification)){
				return;
			}
			switch(notification.getType()){
				case MAIL -> mailNotificationService.notifyRequirementManuallyWatched(notification, userGroupEntity, media);
				case DISCORD, DISCORD_THREAD -> discordNotificationService.notifyRequirementManuallyWatched(notification, userGroupEntity, media);
			}
		}
		catch(MessagingException | UnsupportedEncodingException | InterruptedException | RequestFailedException e){
			throw new NotifyException("Failed to notify media requirement manually marked as watched for group %s".formatted(userGroupEntity), e);
		}
	}
	
	public void notifyRequirementManuallyAbandoned(@NotNull UserGroupEntity userGroupEntity, @NotNull MediaEntity media) throws NotifyException{
		try{
			log.info("Notifying {} has been manually marked as abandoned to {}", media, userGroupEntity);
			var notification = userGroupEntity.getNotification();
			if(Objects.isNull(notification)){
				return;
			}
			switch(notification.getType()){
				case MAIL -> mailNotificationService.notifyRequirementManuallyAbandoned(notification, userGroupEntity, media);
				case DISCORD, DISCORD_THREAD -> discordNotificationService.notifyRequirementManuallyAbandoned(notification, userGroupEntity, media);
			}
		}
		catch(MessagingException | UnsupportedEncodingException | InterruptedException | RequestFailedException e){
			throw new NotifyException("Failed to notify media requirement manually marked as abandoned for group %s".formatted(userGroupEntity), e);
		}
	}
	
	public void notifyMediaAdded(@NotNull GetMetadataResponse metadata, @NotNull GetMetadataResponse rootMetadata) throws NotifyException{
		var ratingKey = switch(metadata.getMediaType()){
			case "movie", "season" -> metadata.getRatingKey();
			case "episode" -> metadata.getParentRatingKey();
			default -> null;
		};
		
		if(Objects.isNull(ratingKey)){
			log.warn("Not notifying media file added, could not determine rating key from {}", metadata);
			return;
		}
		
		var userGroupsRequirement = userGroupRepository.findAllByHasRequirementOnPlex(ratingKey, MediaRequirementStatus.WAITING);
		for(var userGroup : userGroupsRequirement){
			notifyMediaAdded(userGroup, metadata, rootMetadata, true);
		}
		var userGroupsLibrary = userGroupRepository.findAllByWatchesLibrary(metadata.getLibraryName());
		for(var userGroup : userGroupsLibrary){
			if(userGroupsRequirement.contains(userGroup)){
				continue;
			}
			notifyMediaAdded(userGroup, metadata, rootMetadata, false);
		}
	}
	
	private void notifyMediaAdded(@NotNull UserGroupEntity userGroupEntity, @NotNull GetMetadataResponse metadata, @NotNull GetMetadataResponse rootMetadata, boolean ping) throws NotifyException{
		try{
			log.info("Notifying {} has been added to {}", metadata, userGroupEntity);
			var notification = userGroupEntity.getNotificationMediaAdded();
			if(Objects.isNull(notification)){
				return;
			}
			switch(notification.getType()){
				case MAIL -> mailNotificationService.notifyMediaAdded(notification, userGroupEntity, metadata, rootMetadata);
				case DISCORD, DISCORD_THREAD -> discordNotificationService.notifyMediaAdded(notification, userGroupEntity, metadata, rootMetadata, ping);
			}
		}
		catch(MessagingException | UnsupportedEncodingException | RequestFailedException | InterruptedException e){
			throw new NotifyException("Failed to notify media added for group %s".formatted(userGroupEntity), e);
		}
	}
}
