package fr.rakambda.plexdeleter.notify;

import fr.rakambda.plexdeleter.api.RequestFailedException;
import fr.rakambda.plexdeleter.api.tautulli.data.AudioMediaPartStream;
import fr.rakambda.plexdeleter.api.tautulli.data.GetMetadataResponse;
import fr.rakambda.plexdeleter.api.tautulli.data.SubtitlesMediaPartStream;
import fr.rakambda.plexdeleter.config.ApplicationConfiguration;
import fr.rakambda.plexdeleter.config.MailConfiguration;
import fr.rakambda.plexdeleter.service.WatchService;
import fr.rakambda.plexdeleter.storage.entity.MediaEntity;
import fr.rakambda.plexdeleter.storage.entity.MediaType;
import fr.rakambda.plexdeleter.storage.entity.UserGroupEntity;
import jakarta.mail.MessagingException;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class MailNotificationService extends AbstractNotificationService{
	private final JavaMailSender emailSender;
	private final MailConfiguration mailConfiguration;
	private final MessageSource messageSource;
	private final WatchService watchService;
	private final SpringTemplateEngine templateEngine;
	private final String applicationUrl;
	private final String tautulliEndpoint;
	private final String overseerrEndpoint;
	
	@Autowired
	public MailNotificationService(JavaMailSender emailSender, ApplicationConfiguration applicationConfiguration, MessageSource messageSource, WatchService watchService, SpringTemplateEngine templateEngine){
		super();
		this.emailSender = emailSender;
		this.messageSource = messageSource;
		this.mailConfiguration = applicationConfiguration.getMail();
		this.watchService = watchService;
		this.templateEngine = templateEngine;
		this.applicationUrl = applicationConfiguration.getApplicationUrl();
		this.tautulliEndpoint = applicationConfiguration.getTautulli().getEndpoint();
		this.overseerrEndpoint = applicationConfiguration.getOverseerr().getEndpoint();
	}
	
	public void notifyWatchlist(@NotNull UserGroupEntity userGroupEntity, @NotNull Collection<MediaEntity> availableMedia, @NotNull Collection<MediaEntity> notYetAvailableMedia) throws MessagingException, UnsupportedEncodingException{
		var locale = userGroupEntity.getLocaleAsObject();
		var body = new StringBuilder();
		
		if(!availableMedia.isEmpty()){
			writeWatchlistSection(body, "mail.watchlist.body.header.available", locale, userGroupEntity, availableMedia);
			body.append("<hr/>");
		}
		if(!notYetAvailableMedia.isEmpty()){
			writeWatchlistSection(body, "mail.watchlist.body.header.not-yet-available", locale, userGroupEntity, notYetAvailableMedia);
		}
		
		sendMail(userGroupEntity, messageSource.getMessage("mail.watchlist.subject", new Object[0], locale), body.toString());
	}
	
	public void notifyRequirementAdded(@NotNull UserGroupEntity userGroupEntity, @NotNull MediaEntity media) throws MessagingException, UnsupportedEncodingException{
		notifySimple(userGroupEntity, media, "mail.requirement.added.subject");
	}
	
	public void notifyMediaAvailable(@NotNull UserGroupEntity userGroupEntity, @NotNull MediaEntity media) throws MessagingException, UnsupportedEncodingException{
		notifySimple(userGroupEntity, media, "mail.media.available.subject");
	}
	
	public void notifyMediaDeleted(@NotNull UserGroupEntity userGroupEntity, @NotNull MediaEntity media) throws MessagingException, UnsupportedEncodingException{
		notifySimple(userGroupEntity, media, "mail.media.deleted.subject");
	}
	
	public void notifyRequirementManuallyWatched(@NotNull UserGroupEntity userGroupEntity, @NotNull MediaEntity media) throws MessagingException, UnsupportedEncodingException{
		notifySimple(userGroupEntity, media, "mail.requirement.manually-watched.subject");
	}
	
	public void notifyRequirementManuallyAbandoned(@NotNull UserGroupEntity userGroupEntity, @NotNull MediaEntity media) throws MessagingException, UnsupportedEncodingException{
		notifySimple(userGroupEntity, media, "mail.requirement.manually-abandoned.subject");
	}
	
	public void notifyMediaAdded(@NotNull UserGroupEntity userGroupEntity, @NotNull GetMetadataResponse metadata, @NotNull GetMetadataResponse rootMetadata) throws MessagingException, UnsupportedEncodingException{
		var locale = userGroupEntity.getLocaleAsObject();
		
		var context = new Context();
		context.setLocale(userGroupEntity.getLocaleAsObject());
		
		var mediaSeason = switch(metadata.getMediaType()){
			case "episode" -> Stream.of(
							Optional.ofNullable(metadata.getParentMediaIndex())
									.map(i -> messageSource.getMessage("mail.media.added.body.season", new Object[]{i}, locale))
									.orElse(null),
							Optional.ofNullable(metadata.getMediaIndex())
									.map(i -> messageSource.getMessage("mail.media.added.body.episode", new Object[]{i}, locale))
									.orElse(null)
					)
					.filter(Objects::nonNull)
					.collect(Collectors.joining(" - "));
			case "season" -> Optional.ofNullable(metadata.getMediaIndex())
					.map(i -> messageSource.getMessage("mail.media.added.body.season", new Object[]{i}, locale))
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
		
		context.setVariable("mediaTitle", metadata.getFullTitle());
		context.setVariable("mediaSeason", mediaSeason);
		context.setVariable("mediaSummary", metadata.getSummary());
		context.setVariable("mediaReleaseDate", releaseDate);
		context.setVariable("mediaActors", metadata.getActors());
		context.setVariable("mediaGenres", metadata.getGenres());
		context.setVariable("mediaDuration", Duration.ofMillis(metadata.getDuration()).toString().replace("PT", ""));
		context.setVariable("mediaPosterUrl", mediaPoster);
		context.setVariable("mediaAudios", audioLanguages);
		context.setVariable("mediaSubtitles", subtitleLanguages);
		
		sendMail(userGroupEntity,
				messageSource.getMessage("mail.media.added.subject", new Object[0], locale),
				templateEngine.process("mail/media-added.html", context));
	}
	
	private void notifySimple(@NotNull UserGroupEntity userGroupEntity, @NotNull MediaEntity media, @NotNull String subjectKey) throws MessagingException, UnsupportedEncodingException{
		var locale = userGroupEntity.getLocaleAsObject();
		sendMail(userGroupEntity, messageSource.getMessage(subjectKey, new Object[0], locale), getWatchlistMediaText(userGroupEntity, media, locale));
	}
	
	private void sendMail(@NotNull UserGroupEntity userGroupEntity, @NotNull String subject, @NotNull String body) throws MessagingException, UnsupportedEncodingException{
		var mailAddresses = userGroupEntity.getNotificationValue().split(",");
		var mimeMessage = emailSender.createMimeMessage();
		var mailHelper = new MimeMessageHelper(mimeMessage, true, "utf-8");
		mailHelper.setFrom(mailConfiguration.getFromAddress(), mailConfiguration.getFromName());
		mailHelper.setTo(mailAddresses);
		if(Objects.nonNull(mailConfiguration.getBccAddresses()) && !mailConfiguration.getBccAddresses().isEmpty()){
			mailHelper.setBcc(mailConfiguration.getBccAddresses().toArray(new String[0]));
		}
		mailHelper.setSubject(subject);
		mailHelper.setText(body, true);
		emailSender.send(mimeMessage);
	}
	
	private void writeWatchlistSection(@NotNull StringBuilder body, @NotNull String sectionHeaderCode, @NotNull Locale locale, @NotNull UserGroupEntity userGroupEntity, @NotNull Collection<MediaEntity> medias){
		body.append("<h2>");
		body.append(messageSource.getMessage(sectionHeaderCode, new Object[0], locale));
		body.append("</h2>");
		body.append("<ul>");
		medias.stream()
				.sorted(MediaEntity.COMPARATOR_BY_TYPE_THEN_NAME)
				.map(media -> getWatchlistMediaText(userGroupEntity, media, locale))
				.map("<li>%s</li>"::formatted)
				.forEach(body::append);
		body.append("</ul>");
	}
	
	@SneakyThrows(RequestFailedException.class)
	private String getWatchlistMediaText(@NotNull UserGroupEntity userGroupEntity, @NotNull MediaEntity media, @NotNull Locale locale){
		var sb = new StringBuilder();
		sb.append(switch(media.getType()){
			case MOVIE -> messageSource.getMessage("mail.watchlist.body.media.movie", new Object[]{
					media.getName(),
					}, locale);
			case SEASON -> messageSource.getMessage("mail.watchlist.body.media.series", new Object[]{
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
				sb.append(messageSource.getMessage("mail.watchlist.body.media.series.episodes", new Object[]{String.join(", ", episodes)}, locale));
			}
		}
		
		if(Objects.nonNull(media.getOverseerrId())){
			sb.append(" | ");
			sb.append("<a href='");
			sb.append(overseerrEndpoint);
			sb.append("/");
			sb.append(switch(media.getType()){
				case MOVIE -> "movie";
				case SEASON -> "tv";
			});
			sb.append("/");
			sb.append(media.getOverseerrId());
			sb.append("'><img style='max-height: 15px; max-width: 15px;' src='https://raw.githubusercontent.com/sct/overseerr/develop/public/os_logo_filled.png'/></a>");
		}
		
		return sb.toString();
	}
}
