package fr.rakambda.plexdeleter.notify;

import fr.rakambda.plexdeleter.api.RequestFailedException;
import fr.rakambda.plexdeleter.storage.entity.MediaEntity;
import fr.rakambda.plexdeleter.storage.entity.MediaRequirementStatus;
import fr.rakambda.plexdeleter.storage.entity.UserGroupEntity;
import fr.rakambda.plexdeleter.storage.repository.UserGroupRepository;
import jakarta.mail.MessagingException;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.UnsupportedEncodingException;
import java.util.Collection;

@Service
public class NotificationService{
	private final MailNotificationService mailNotificationService;
	private final DiscordThreadNotificationService discordThreadNotificationService;
	private final UserGroupRepository userGroupRepository;
	
	@Autowired
	public NotificationService(MailNotificationService mailNotificationService, DiscordThreadNotificationService discordThreadNotificationService, UserGroupRepository userGroupRepository){
		this.mailNotificationService = mailNotificationService;
		this.discordThreadNotificationService = discordThreadNotificationService;
		this.userGroupRepository = userGroupRepository;
	}
	
	public void notifyWatchlist(@NotNull UserGroupEntity userGroupEntity, @NotNull Collection<MediaEntity> availableMedia, @NotNull Collection<MediaEntity> notYetAvailableMedia) throws NotifyException{
		try{
			if(availableMedia.isEmpty() && notYetAvailableMedia.isEmpty()){
				return;
			}
			switch(userGroupEntity.getNotificationType()){
				case MAIL -> mailNotificationService.notifyWatchlist(userGroupEntity, availableMedia, notYetAvailableMedia);
				case DISCORD_THREAD -> discordThreadNotificationService.notifyWatchlist(userGroupEntity, availableMedia, notYetAvailableMedia);
			}
		}
		catch(MessagingException | UnsupportedEncodingException | InterruptedException | RequestFailedException e){
			throw new NotifyException("Failed to notify watchlist for group %s".formatted(userGroupEntity), e);
		}
	}
	
	public void notifyRequirementAdded(@NotNull UserGroupEntity userGroupEntity, @NotNull MediaEntity media) throws NotifyException{
		try{
			switch(userGroupEntity.getNotificationType()){
				case MAIL -> mailNotificationService.notifyRequirementAdded(userGroupEntity, media);
				case DISCORD_THREAD -> discordThreadNotificationService.notifyRequirementAdded(userGroupEntity, media);
			}
		}
		catch(MessagingException | UnsupportedEncodingException | InterruptedException | RequestFailedException e){
			throw new NotifyException("Failed to notify media requirement added for group %s".formatted(userGroupEntity), e);
		}
	}
	
	public void notifyMediaAvailable(@NotNull MediaEntity media) throws NotifyException{
		var userGroups = userGroupRepository.findAllByHasRequirementOn(media.getId(), MediaRequirementStatus.WAITING);
		for(var userGroup : userGroups){
			notifyMediaAvailable(userGroup, media);
		}
	}
	
	public void notifyMediaAvailable(@NotNull UserGroupEntity userGroupEntity, @NotNull MediaEntity media) throws NotifyException{
		try{
			switch(userGroupEntity.getNotificationType()){
				case MAIL -> mailNotificationService.notifyMediaAvailable(userGroupEntity, media);
				case DISCORD_THREAD -> discordThreadNotificationService.notifyMediaAvailable(userGroupEntity, media);
			}
		}
		catch(MessagingException | UnsupportedEncodingException | InterruptedException | RequestFailedException e){
			throw new NotifyException("Failed to notify media added for group %s".formatted(userGroupEntity), e);
		}
	}
	
	public void notifyMediaDeleted(@NotNull MediaEntity media) throws NotifyException{
		var userGroups = userGroupRepository.findAllByHasRequirementOn(media.getId(), MediaRequirementStatus.WAITING);
		for(var userGroup : userGroups){
			notifyMediaDeleted(userGroup, media);
		}
	}
	
	public void notifyMediaDeleted(@NotNull UserGroupEntity userGroupEntity, @NotNull MediaEntity media) throws NotifyException{
		try{
			switch(userGroupEntity.getNotificationType()){
				case MAIL -> mailNotificationService.notifyMediaDeleted(userGroupEntity, media);
				case DISCORD_THREAD -> discordThreadNotificationService.notifyMediaDeleted(userGroupEntity, media);
			}
		}
		catch(MessagingException | UnsupportedEncodingException | InterruptedException | RequestFailedException e){
			throw new NotifyException("Failed to notify media deleted for group %s".formatted(userGroupEntity), e);
		}
	}
	
	public void notifyRequirementManuallyWatched(@NotNull UserGroupEntity userGroupEntity, @NotNull MediaEntity media) throws NotifyException{
		try{
			switch(userGroupEntity.getNotificationType()){
				case MAIL -> mailNotificationService.notifyRequirementManuallyWatched(userGroupEntity, media);
				case DISCORD_THREAD -> discordThreadNotificationService.notifyRequirementManuallyWatched(userGroupEntity, media);
			}
		}
		catch(MessagingException | UnsupportedEncodingException | InterruptedException | RequestFailedException e){
			throw new NotifyException("Failed to notify media requirement manually marked as watched for group %s".formatted(userGroupEntity), e);
		}
	}
	
	public void notifyRequirementManuallyAbandoned(@NotNull UserGroupEntity userGroupEntity, @NotNull MediaEntity media) throws NotifyException{
		try{
			switch(userGroupEntity.getNotificationType()){
				case MAIL -> mailNotificationService.notifyRequirementManuallyAbandoned(userGroupEntity, media);
				case DISCORD_THREAD -> discordThreadNotificationService.notifyRequirementManuallyAbandoned(userGroupEntity, media);
			}
		}
		catch(MessagingException | UnsupportedEncodingException | InterruptedException | RequestFailedException e){
			throw new NotifyException("Failed to notify media requirement manually marked as abandoned for group %s".formatted(userGroupEntity), e);
		}
	}
}
