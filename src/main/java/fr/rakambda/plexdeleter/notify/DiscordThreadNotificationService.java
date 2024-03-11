package fr.rakambda.plexdeleter.notify;

import fr.rakambda.plexdeleter.api.RequestFailedException;
import fr.rakambda.plexdeleter.api.discord.DiscordWebhookService;
import fr.rakambda.plexdeleter.api.discord.data.WebhookMessage;
import fr.rakambda.plexdeleter.config.ApplicationConfiguration;
import fr.rakambda.plexdeleter.service.WatchService;
import fr.rakambda.plexdeleter.storage.entity.MediaEntity;
import fr.rakambda.plexdeleter.storage.entity.MediaType;
import fr.rakambda.plexdeleter.storage.entity.UserGroupEntity;
import jakarta.mail.MessagingException;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
public class DiscordThreadNotificationService{
	private final DiscordWebhookService discordWebhookService;
	private final MessageSource messageSource;
	private final WatchService watchService;
	private final String overseerrUrl;
	
	@Autowired
	public DiscordThreadNotificationService(DiscordWebhookService discordWebhookService, ApplicationConfiguration applicationConfiguration, MessageSource messageSource, WatchService watchService){
		this.discordWebhookService = discordWebhookService;
		this.messageSource = messageSource;
		this.overseerrUrl = applicationConfiguration.getOverseerr().getEndpoint();
		this.watchService = watchService;
	}
	
	public void notifyWatchlist(@NotNull UserGroupEntity userGroupEntity, @NotNull Collection<MediaEntity> availableMedia, @NotNull Collection<MediaEntity> notYetAvailableMedia) throws MessagingException, UnsupportedEncodingException, InterruptedException, RequestFailedException{
		var locale = userGroupEntity.getLocaleAsObject();
		var params = userGroupEntity.getNotificationValue().split(",");
		var discordUserId = params[0];
		var discordUrl = params[1];
		
		var threadId = Optional.ofNullable(discordWebhookService.sendWebhookMessage(discordUrl, WebhookMessage.builder()
								.threadName(messageSource.getMessage("discord.watchlist.subject", new Object[0], locale))
								.content("<@%s>".formatted(discordUserId))
								.build())
						.getChannelId())
				.orElseThrow(() -> new RequestFailedException("Couldn't get new thread channel id"));
		
		if(!availableMedia.isEmpty()){
			writeWatchlistSection(discordUrl, threadId, "discord.watchlist.body.header.available", locale, userGroupEntity, availableMedia);
			discordWebhookService.sendWebhookMessage(discordUrl, threadId, WebhookMessage.builder().content("---").build());
		}
		if(!notYetAvailableMedia.isEmpty()){
			writeWatchlistSection(discordUrl, threadId, "discord.watchlist.body.header.not-yet-available", locale, userGroupEntity, notYetAvailableMedia);
		}
	}
	
	public void notifyRequirementAdded(@NotNull UserGroupEntity userGroupEntity, @NotNull MediaEntity media) throws MessagingException, UnsupportedEncodingException, RequestFailedException, InterruptedException{
		notifySimple(userGroupEntity, media, "discord.requirement.added.subject");
	}
	
	public void notifyMediaAvailable(@NotNull UserGroupEntity userGroupEntity, @NotNull MediaEntity media) throws RequestFailedException, InterruptedException{
		notifySimple(userGroupEntity, media, "discord.media.available.subject");
	}
	
	public void notifyMediaDeleted(@NotNull UserGroupEntity userGroupEntity, @NotNull MediaEntity media) throws RequestFailedException, InterruptedException{
		notifySimple(userGroupEntity, media, "discord.media.deleted.subject");
	}
	
	public void notifyRequirementManuallyWatched(@NotNull UserGroupEntity userGroupEntity, @NotNull MediaEntity media) throws MessagingException, UnsupportedEncodingException, RequestFailedException, InterruptedException{
		notifySimple(userGroupEntity, media, "discord.requirement.manually-watched.subject");
	}
	
	public void notifyRequirementManuallyAbandoned(@NotNull UserGroupEntity userGroupEntity, @NotNull MediaEntity media) throws RequestFailedException, InterruptedException{
		notifySimple(userGroupEntity, media, "discord.requirement.manually-abandoned.subject");
	}
	
	private void notifySimple(@NotNull UserGroupEntity userGroupEntity, @NotNull MediaEntity media, @NotNull String subjectKey) throws RequestFailedException, InterruptedException{
		var locale = userGroupEntity.getLocaleAsObject();
		var params = userGroupEntity.getNotificationValue().split(",");
		var discordUserId = params[0];
		var discordUrl = params[1];
		
		discordWebhookService.sendWebhookMessage(discordUrl, WebhookMessage.builder()
				.threadName(messageSource.getMessage(subjectKey, new Object[0], locale))
				.content("<@%s>\n%s".formatted(discordUserId, getWatchlistMediaText(userGroupEntity, media, locale)))
				.build());
	}
	
	private void writeWatchlistSection(@NotNull String discordUrl, long threadId, @NotNull String sectionHeaderCode, @NotNull Locale locale, @NotNull UserGroupEntity userGroupEntity, @NotNull Collection<MediaEntity> medias) throws RequestFailedException, InterruptedException{
		discordWebhookService.sendWebhookMessage(discordUrl, threadId, WebhookMessage.builder().content("# %s\n".formatted(messageSource.getMessage(sectionHeaderCode, new Object[0], locale))).build());
		var messages = medias.stream()
				.sorted(MediaEntity.COMPARATOR_BY_TYPE_THEN_NAME)
				.map(media -> getWatchlistMediaText(userGroupEntity, media, locale))
				.toList();
		for(var message : messages){
			discordWebhookService.sendWebhookMessage(discordUrl, threadId, WebhookMessage.builder().content("* %s".formatted(message)).build());
		}
	}
	
	@SneakyThrows(RequestFailedException.class)
	private String getWatchlistMediaText(@NotNull UserGroupEntity userGroupEntity, @NotNull MediaEntity media, @NotNull Locale locale){
		var sb = new StringBuilder();
		sb.append(switch(media.getType()){
			case MOVIE -> messageSource.getMessage("discord.watchlist.body.media.movie", new Object[]{
					media.getName(),
					}, locale);
			case SEASON -> messageSource.getMessage("discord.watchlist.body.media.series", new Object[]{
					media.getName(),
					media.getIndex(),
					}, locale);
		});
		
		if(media.getType() == MediaType.SEASON && Objects.nonNull(media.getPlexId())){
			var episodes = watchService.getGroupWatchHistory(userGroupEntity, media).entrySet().stream()
					.filter(entry -> entry.getValue().stream().allMatch(r -> Objects.equals(r.getWatchedStatus(), 0)))
					.map(Map.Entry::getKey)
					.map(String::valueOf)
					.toList();
			if(!episodes.isEmpty()){
				sb.append(" | ");
				sb.append(messageSource.getMessage("discord.watchlist.body.media.series.episodes", new Object[]{String.join(", ", episodes)}, locale));
			}
		}
		
		if(Objects.nonNull(media.getOverseerrId())){
			sb.append(" | ");
			sb.append("[Overseerr](");
			sb.append(overseerrUrl);
			sb.append("/");
			sb.append(switch(media.getType()){
				case MOVIE -> "movie";
				case SEASON -> "tv";
			});
			sb.append("/");
			sb.append(media.getOverseerrId());
			sb.append(")");
		}
		
		return sb.toString();
	}
}
