package fr.rakambda.plexdeleter.notify;

import fr.rakambda.plexdeleter.storage.entity.MediaEntity;
import fr.rakambda.plexdeleter.storage.entity.UserGroupEntity;
import jakarta.mail.MessagingException;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.UnsupportedEncodingException;
import java.util.Collection;

@Service
public class NotificationService{
	private final MailNotificationService mailNotificationService;
	
	@Autowired
	public NotificationService(MailNotificationService mailNotificationService){
		this.mailNotificationService = mailNotificationService;
	}
	
	public void notifyWatchlist(@NotNull UserGroupEntity userGroupEntity, @NotNull Collection<MediaEntity> availableMedia, @NotNull Collection<MediaEntity> notYetAvailableMedia) throws NotifyException{
		try{
			switch(userGroupEntity.getNotificationType()){
				case MAIL -> mailNotificationService.notifyWatchlist(userGroupEntity, availableMedia, notYetAvailableMedia);
				default -> {
				}
			}
		}
		catch(MessagingException | UnsupportedEncodingException e){
			throw new NotifyException("Failed to notify watchlist for group %s".formatted(userGroupEntity), e);
		}
	}
}
