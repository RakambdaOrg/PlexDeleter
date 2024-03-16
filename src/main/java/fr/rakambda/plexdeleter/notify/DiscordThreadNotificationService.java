package fr.rakambda.plexdeleter.notify;

import fr.rakambda.plexdeleter.api.RequestFailedException;
import fr.rakambda.plexdeleter.api.discord.DiscordWebhookService;
import fr.rakambda.plexdeleter.api.discord.data.Embed;
import fr.rakambda.plexdeleter.api.discord.data.Field;
import fr.rakambda.plexdeleter.api.discord.data.Image;
import fr.rakambda.plexdeleter.api.discord.data.WebhookMessage;
import fr.rakambda.plexdeleter.api.tautulli.data.AudioMediaPartStream;
import fr.rakambda.plexdeleter.api.tautulli.data.GetMetadataResponse;
import fr.rakambda.plexdeleter.api.tautulli.data.SubtitlesMediaPartStream;
import fr.rakambda.plexdeleter.config.ApplicationConfiguration;
import fr.rakambda.plexdeleter.service.WatchService;
import fr.rakambda.plexdeleter.storage.entity.MediaEntity;
import fr.rakambda.plexdeleter.storage.entity.UserGroupEntity;
import jakarta.mail.MessagingException;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class DiscordThreadNotificationService extends AbstractNotificationService{
	private static final int FLAG_SUPPRESS_EMBEDS = 1 << 2;
	
	private final DiscordWebhookService discordWebhookService;
	private final MessageSource messageSource;
	private final String tautulliEndpoint;
	private final String overseerrEndpoint;
	
	@Autowired
	public DiscordThreadNotificationService(DiscordWebhookService discordWebhookService, ApplicationConfiguration applicationConfiguration, MessageSource messageSource, WatchService watchService){
		super(watchService);
		this.discordWebhookService = discordWebhookService;
		this.messageSource = messageSource;
		this.tautulliEndpoint = applicationConfiguration.getTautulli().getEndpoint();
		this.overseerrEndpoint = applicationConfiguration.getOverseerr().getEndpoint();
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
	
	public void notifyMediaAdded(@NotNull UserGroupEntity userGroupEntity, @NotNull GetMetadataResponse metadata, @NotNull GetMetadataResponse rootMetadata) throws RequestFailedException, InterruptedException{
		var locale = userGroupEntity.getLocaleAsObject();
		var params = userGroupEntity.getNotificationValue().split(",");
		var discordUserId = params[0];
		var discordUrl = params[1];
		
		var context = new Context();
		context.setLocale(userGroupEntity.getLocaleAsObject());
		
		var mediaSeason = switch(metadata.getMediaType()){
			case "episode" -> Stream.of(
							Optional.ofNullable(metadata.getParentMediaIndex())
									.map(i -> messageSource.getMessage("discord.media.added.body.season", new Object[]{i}, locale))
									.orElse(null),
							Optional.ofNullable(metadata.getMediaIndex())
									.map(i -> messageSource.getMessage("discord.media.added.body.episode", new Object[]{i}, locale))
									.orElse(null)
					)
					.filter(Objects::nonNull)
					.collect(Collectors.joining(" - "));
			case "season" -> Optional.ofNullable(metadata.getMediaIndex())
					.map(i -> messageSource.getMessage("discord.media.added.body.season", new Object[]{i}, locale))
					.orElse(null);
			default -> null;
		};
		var releaseDate = Optional.ofNullable(metadata.getOriginallyAvailableAt())
				.map(DATE_FORMATTER::format)
				.orElse(null);
		var mediaPoster = "%s/pms_image_proxy?img=%s&rating_key=%d&width=%d&height=%d&fallback=poster".formatted(
				tautulliEndpoint,
				Optional.ofNullable(rootMetadata.getThumb()).map(s -> URLEncoder.encode(s, StandardCharsets.UTF_8)).orElse(""),
				rootMetadata.getRatingKey(),
				222,
				333);
		var audioLanguages = getMediaStreams(metadata, AudioMediaPartStream.class)
				.map(AudioMediaPartStream::getAudioLanguageCode)
				.flatMap(code -> getLanguageName(code, locale))
				.toList();
		var subtitleLanguages = getMediaStreams(metadata, SubtitlesMediaPartStream.class)
				.map(SubtitlesMediaPartStream::getSubtitleLanguageCode)
				.flatMap(code -> getLanguageName(code, locale))
				.toList();
		
		discordWebhookService.sendWebhookMessage(discordUrl, WebhookMessage.builder()
				.threadName(messageSource.getMessage("discord.media.added.subject", new Object[0], locale))
				.content("<@%s>".formatted(discordUserId))
				.embeds(List.of(Embed.builder()
						.title(metadata.getFullTitle())
						.description(mediaSeason)
						.image(Image.builder()
								.url(mediaPoster)
								.build())
						.field(Field.builder()
								.name(messageSource.getMessage("discord.media.available.body.summary", new Object[0], locale))
								.value(metadata.getSummary())
								.build())
						.field(Field.builder()
								.name(messageSource.getMessage("discord.media.available.body.release-date", new Object[0], locale))
								.value(releaseDate)
								.build())
						.field(Field.builder()
								.name(messageSource.getMessage("discord.media.available.body.actors", new Object[0], locale))
								.value(metadata.getActors().stream().limit(5).collect(Collectors.joining(", ")))
								.build())
						.field(Field.builder()
								.name(messageSource.getMessage("discord.media.available.body.genres", new Object[0], locale))
								.value(String.join(", ", metadata.getGenres()))
								.build())
						.field(Field.builder()
								.name(messageSource.getMessage("discord.media.available.body.length", new Object[0], locale))
								.value(Duration.ofMillis(metadata.getDuration()).toString().replace("PT", ""))
								.build())
						.field(Field.builder()
								.name(messageSource.getMessage("discord.media.available.body.audios", new Object[0], locale))
								.value(String.join(", ", audioLanguages))
								.build())
						.field(Field.builder()
								.name(messageSource.getMessage("discord.media.available.body.subtitles", new Object[0], locale))
								.value(String.join(", ", subtitleLanguages))
								.build())
						.build()))
				.build());
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
			discordWebhookService.sendWebhookMessage(discordUrl, threadId, WebhookMessage.builder()
					.content("* %s".formatted(message))
					.flags(FLAG_SUPPRESS_EMBEDS)
					.build());
		}
	}
	
	@SneakyThrows(RequestFailedException.class)
	private String getWatchlistMediaText(@NotNull UserGroupEntity userGroupEntity, @NotNull MediaEntity media, @NotNull Locale locale){
		var sb = new StringBuilder();
		sb.append(messageSource.getMessage(getTypeKey(media), new Object[]{
				media.getName(),
				media.getIndex(),
				}, locale));
		
		var episodes = getEpisodes(media, userGroupEntity);
		if(!episodes.isEmpty()){
			sb.append(" | ");
			sb.append(messageSource.getMessage("discord.watchlist.body.media.series.episodes", new Object[]{String.join(", ", episodes)}, locale));
		}
		
		if(Objects.nonNull(media.getOverseerrId())){
			sb.append(" | ");
			sb.append("[Overseerr](");
			sb.append(overseerrEndpoint);
			sb.append("/");
			sb.append(media.getType().getOverseerrType().getValue());
			sb.append("/");
			sb.append(media.getOverseerrId());
			sb.append(")");
		}
		
		return sb.toString();
	}
}
