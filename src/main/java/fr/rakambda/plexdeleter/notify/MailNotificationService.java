package fr.rakambda.plexdeleter.notify;

import fr.rakambda.plexdeleter.api.tautulli.data.AudioMediaPartStream;
import fr.rakambda.plexdeleter.api.tautulli.data.GetMetadataResponse;
import fr.rakambda.plexdeleter.api.tautulli.data.SubtitlesMediaPartStream;
import fr.rakambda.plexdeleter.config.ApplicationConfiguration;
import fr.rakambda.plexdeleter.config.MailConfiguration;
import fr.rakambda.plexdeleter.service.WatchService;
import fr.rakambda.plexdeleter.storage.entity.MediaAvailability;
import fr.rakambda.plexdeleter.storage.entity.MediaEntity;
import fr.rakambda.plexdeleter.storage.entity.MediaRequirementEntity;
import fr.rakambda.plexdeleter.storage.entity.NotificationEntity;
import fr.rakambda.plexdeleter.storage.entity.UserGroupEntity;
import jakarta.mail.MessagingException;
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
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class MailNotificationService extends AbstractNotificationService{
	private final JavaMailSender emailSender;
	private final MailConfiguration mailConfiguration;
	private final MessageSource messageSource;
	private final SpringTemplateEngine templateEngine;
	private final String tautulliEndpoint;
	private final String overseerrEndpoint;
	
	@Autowired
	public MailNotificationService(JavaMailSender emailSender, ApplicationConfiguration applicationConfiguration, MessageSource messageSource, WatchService watchService, SpringTemplateEngine templateEngine){
		super(watchService);
		this.emailSender = emailSender;
		this.messageSource = messageSource;
		this.mailConfiguration = applicationConfiguration.getMail();
		this.templateEngine = templateEngine;
		this.tautulliEndpoint = applicationConfiguration.getTautulli().getEndpoint();
		this.overseerrEndpoint = applicationConfiguration.getOverseerr().getEndpoint();
	}
	
	public void notifyWatchlist(@NotNull NotificationEntity notification, @NotNull UserGroupEntity userGroupEntity, @NotNull Collection<MediaRequirementEntity> requirements) throws MessagingException, UnsupportedEncodingException{
		var locale = userGroupEntity.getLocaleAsObject();
		var context = new Context();
		context.setLocale(userGroupEntity.getLocaleAsObject());
		
		context.setVariable("service", this);
		context.setVariable("overseerrEndpoint", overseerrEndpoint);
		context.setVariable("userGroup", userGroupEntity);
		context.setVariable("availableMedias", requirements.stream()
				.map(MediaRequirementEntity::getMedia)
				.filter(m -> Objects.equals(m.getAvailability(), MediaAvailability.DOWNLOADED))
				.toList());
		context.setVariable("downloadingMedias", requirements.stream()
				.map(MediaRequirementEntity::getMedia)
				.filter(m -> Objects.equals(m.getAvailability(), MediaAvailability.DOWNLOADING))
				.toList());
		context.setVariable("notYetAvailableMedias", requirements.stream()
				.map(MediaRequirementEntity::getMedia)
				.filter(m -> Objects.equals(m.getAvailability(), MediaAvailability.WAITING))
				.toList());
		
		sendMail(notification,
				messageSource.getMessage("mail.watchlist.subject", new Object[0], locale),
				templateEngine.process("mail/watchlist.html", context));
	}
	
	public void notifyRequirementAdded(@NotNull NotificationEntity notification, @NotNull UserGroupEntity userGroupEntity, @NotNull MediaEntity media) throws MessagingException, UnsupportedEncodingException{
		notifySimple(notification, userGroupEntity, media, "mail.requirement.added.subject");
	}
	
	public void notifyMediaAvailable(@NotNull NotificationEntity notification, @NotNull UserGroupEntity userGroupEntity, @NotNull MediaEntity media) throws MessagingException, UnsupportedEncodingException{
		notifySimple(notification, userGroupEntity, media, "mail.media.available.subject");
	}
	
	public void notifyMediaDeleted(@NotNull NotificationEntity notification, @NotNull UserGroupEntity userGroupEntity, @NotNull MediaEntity media) throws MessagingException, UnsupportedEncodingException{
		notifySimple(notification, userGroupEntity, media, "mail.media.deleted.subject");
	}
	
	public void notifyRequirementManuallyWatched(@NotNull NotificationEntity notification, @NotNull UserGroupEntity userGroupEntity, @NotNull MediaEntity media) throws MessagingException, UnsupportedEncodingException{
		notifySimple(notification, userGroupEntity, media, "mail.requirement.manually-watched.subject");
	}
	
	public void notifyRequirementManuallyAbandoned(@NotNull NotificationEntity notification, @NotNull UserGroupEntity userGroupEntity, @NotNull MediaEntity media) throws MessagingException, UnsupportedEncodingException{
		notifySimple(notification, userGroupEntity, media, "mail.requirement.manually-abandoned.subject");
	}
	
	public void notifyMediaAdded(@NotNull NotificationEntity notification, @NotNull UserGroupEntity userGroupEntity, @NotNull GetMetadataResponse metadata, @NotNull GetMetadataResponse rootMetadata) throws MessagingException, UnsupportedEncodingException{
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
		context.setVariable("mediaDuration", getMediaDuration(Duration.ofMillis(metadata.getDuration())));
		context.setVariable("mediaPosterUrl", mediaPoster);
		context.setVariable("mediaAudios", audioLanguages);
		context.setVariable("mediaSubtitles", subtitleLanguages);
		
		sendMail(notification,
				messageSource.getMessage("mail.media.added.subject", new Object[0], locale),
				templateEngine.process("mail/media-added.html", context));
	}
	
	private void notifySimple(@NotNull NotificationEntity notification, @NotNull UserGroupEntity userGroupEntity, @NotNull MediaEntity media, @NotNull String subjectKey) throws MessagingException, UnsupportedEncodingException{
		var locale = userGroupEntity.getLocaleAsObject();
		var context = new Context();
		context.setLocale(userGroupEntity.getLocaleAsObject());
		
		context.setVariable("service", this);
		context.setVariable("media", media);
		context.setVariable("overseerrEndpoint", overseerrEndpoint);
		context.setVariable("userGroup", userGroupEntity);
		
		sendMail(notification,
				messageSource.getMessage(subjectKey, new Object[0], locale),
				templateEngine.process("mail/single-media.html", context));
	}
	
	private void sendMail(@NotNull NotificationEntity notification, @NotNull String subject, @NotNull String body) throws MessagingException, UnsupportedEncodingException{
		var mailAddresses = notification.getValue().split(",");
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
}
