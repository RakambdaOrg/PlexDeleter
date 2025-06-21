package fr.rakambda.plexdeleter.notify;

import fr.rakambda.plexdeleter.api.tautulli.data.AudioMediaPartStream;
import fr.rakambda.plexdeleter.api.tautulli.data.MediaInfo;
import fr.rakambda.plexdeleter.api.tautulli.data.SubtitlesMediaPartStream;
import fr.rakambda.plexdeleter.config.ApplicationConfiguration;
import fr.rakambda.plexdeleter.config.MailConfiguration;
import fr.rakambda.plexdeleter.notify.context.MediaMetadataContext;
import fr.rakambda.plexdeleter.service.ThymeleafService;
import fr.rakambda.plexdeleter.service.WatchService;
import fr.rakambda.plexdeleter.storage.entity.MediaEntity;
import fr.rakambda.plexdeleter.storage.entity.MediaRequirementEntity;
import fr.rakambda.plexdeleter.storage.entity.NotificationEntity;
import fr.rakambda.plexdeleter.storage.entity.UserGroupEntity;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import java.io.UnsupportedEncodingException;
import java.time.Duration;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
public class MailNotificationService extends AbstractNotificationService{
	private final JavaMailSender emailSender;
	private final MailConfiguration mailConfiguration;
	private final MessageSource messageSource;
	private final SpringTemplateEngine templateEngine;
	private final ThymeleafService thymeleafService;
	
	@Autowired
	public MailNotificationService(JavaMailSender emailSender, ApplicationConfiguration applicationConfiguration, MessageSource messageSource, WatchService watchService, SpringTemplateEngine templateEngine, ThymeleafService thymeleafService){
		super(watchService, messageSource);
		this.emailSender = emailSender;
		this.messageSource = messageSource;
		this.mailConfiguration = applicationConfiguration.getMail();
		this.templateEngine = templateEngine;
		this.thymeleafService = thymeleafService;
	}
	
	public void notifyWatchlist(@NotNull NotificationEntity notification, @NotNull UserGroupEntity userGroupEntity, @NotNull Collection<MediaRequirementEntity> requirements) throws MessagingException, UnsupportedEncodingException{
		var locale = userGroupEntity.getLocaleAsObject();
		var context = new Context();
		context.setLocale(userGroupEntity.getLocaleAsObject());
		
		var overseerrLogoData = getOverseerrLogoBytes();
		var plexLogoData = getPlexLogoBytes();
		var tmdbLogoData = getTmdbLogoBytes();
		var tvdbLogoData = getTvdbLogoBytes();
		var traktLogoData = getTraktLogoBytes();
		
		var overseerrLogoResourceName = "overseerrLogoResourceName";
		var plexLogoResourceName = "plexLogoResourceName";
		var tmdbLogoResourceName = "tmdbLogoResourceName";
		var tvdbLogoResourceName = "tvdbLogoResourceName";
		var traktLogoResourceName = "traktLogoResourceName";
		
		var availableMedia = requirements.stream()
				.map(MediaRequirementEntity::getMedia)
				.filter(m -> m.getStatus().isFullyDownloaded())
				.sorted(MediaEntity.COMPARATOR_BY_TYPE_THEN_NAME_THEN_INDEX)
				.toList();
		var downloadingMedia = requirements.stream()
				.map(MediaRequirementEntity::getMedia)
				.filter(m -> m.getStatus().isDownloadStarted() && !m.getStatus().isFullyDownloaded())
				.sorted(MediaEntity.COMPARATOR_BY_TYPE_THEN_NAME_THEN_INDEX)
				.toList();
		var notYetAvailableMedia = requirements.stream()
				.map(MediaRequirementEntity::getMedia)
				.filter(m -> !m.getStatus().isDownloadStarted())
				.sorted(MediaEntity.COMPARATOR_BY_TYPE_THEN_NAME_THEN_INDEX)
				.toList();
		
		if(availableMedia.isEmpty() && downloadingMedia.isEmpty()){
			log.info("No medias eligible to notify");
			return;
		}
		
		context.setVariable("service", this);
		context.setVariable("thymeleafService", thymeleafService);
		context.setVariable("userGroup", userGroupEntity);
		context.setVariable("availableMedias", availableMedia);
		context.setVariable("downloadingMedias", downloadingMedia);
		context.setVariable("notYetAvailableMedias", notYetAvailableMedia);
		context.setVariable("overseerrLogoResourceName", overseerrLogoData.isPresent() ? overseerrLogoResourceName : null);
		context.setVariable("plexLogoResourceName", plexLogoData.isPresent() ? plexLogoResourceName : null);
		context.setVariable("tmdbLogoResourceName", tmdbLogoData.isPresent() ? tmdbLogoResourceName : null);
		context.setVariable("tvdbLogoResourceName", tvdbLogoData.isPresent() ? tvdbLogoResourceName : null);
		context.setVariable("traktLogoResourceName", traktLogoData.isPresent() ? traktLogoResourceName : null);
		
		sendMail(notification, message -> {
			message.setSubject(messageSource.getMessage("mail.watchlist.subject", new Object[0], locale));
			message.setText(templateEngine.process("mail/watchlist.html", context), true);
			if(overseerrLogoData.isPresent()){
				message.addInline(overseerrLogoResourceName, new ByteArrayResource(overseerrLogoData.get(), "Overseerr logo"), "image/png");
			}
			if(plexLogoData.isPresent()){
				message.addInline(plexLogoResourceName, new ByteArrayResource(plexLogoData.get(), "Plex logo"), "image/png");
			}
			if(tmdbLogoData.isPresent()){
				message.addInline(tmdbLogoResourceName, new ByteArrayResource(tmdbLogoData.get(), "Tmdb logo"), "image/png");
			}
			if(tvdbLogoData.isPresent()){
				message.addInline(tvdbLogoResourceName, new ByteArrayResource(tvdbLogoData.get(), "Tvdb logo"), "image/png");
			}
			if(traktLogoData.isPresent()){
				message.addInline(traktLogoResourceName, new ByteArrayResource(traktLogoData.get(), "Trakt logo"), "image/png");
			}
		});
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
	
	public void notifyMediaAdded(@NotNull NotificationEntity notification, @NotNull UserGroupEntity userGroupEntity, @Nullable MediaEntity media, @NotNull MediaMetadataContext mediaMetadataContext) throws MessagingException, UnsupportedEncodingException{
		var locale = userGroupEntity.getLocaleAsObject();
		var metadata = mediaMetadataContext.getMetadata();
		
		var context = new Context();
		context.setLocale(userGroupEntity.getLocaleAsObject());
		
		var mediaSeason = getMediaSeason(metadata, locale);
		var releaseDate = Optional.ofNullable(metadata.getOriginallyAvailableAt())
				.map(DATE_FORMATTER::format)
				.orElse(null);
		var audioLanguages = getMediaStreams(metadata, AudioMediaPartStream.class)
				.map(AudioMediaPartStream::getAudioLanguageCode)
				.distinct()
				.map(s -> Objects.equals(s, "") ? "unknown" : s)
				.map("locale.%s"::formatted)
				.toList();
		var subtitleLanguages = getMediaStreams(metadata, SubtitlesMediaPartStream.class)
				.map(SubtitlesMediaPartStream::getSubtitleLanguageCode)
				.distinct()
				.map(s -> Objects.equals(s, "") ? "unknown" : s)
				.map("locale.%s"::formatted)
				.toList();
		var resolutions = metadata.getMediaInfo().stream()
				.map(MediaInfo::getVideoFullResolution)
				.filter(Objects::nonNull)
				.distinct()
				.toList();
		var bitrates = metadata.getMediaInfo().stream()
				.map(MediaInfo::getBitrate)
				.filter(Objects::nonNull)
				.distinct()
				.toList();
		
		var posterData = mediaMetadataContext.getPosterData();
		var mediaPosterResourceName = "mediaPosterResourceName";
		
		var suggestAddRequirementId = Optional.ofNullable(media)
				.filter(m -> m.getRequirements().stream()
						.map(MediaRequirementEntity::getGroup)
						.map(UserGroupEntity::getId)
						.noneMatch(group -> Objects.equals(group, userGroupEntity.getId())))
				.map(MediaEntity::getId)
				.orElse(null);
		
		context.setVariable("thymeleafService", thymeleafService);
		context.setVariable("mediaTitle", mediaMetadataContext.getTitle(locale).orElseGet(metadata::getFullTitle));
		context.setVariable("mediaSeason", mediaSeason);
		context.setVariable("mediaSummary", mediaMetadataContext.getSummary(locale).orElseGet(metadata::getSummary));
		context.setVariable("mediaReleaseDate", releaseDate);
		context.setVariable("mediaActors", metadata.getActors());
		context.setVariable("mediaGenres", mediaMetadataContext.getGenres(messageSource, locale).orElseGet(metadata::getGenres));
		context.setVariable("mediaDuration", getMediaDuration(Duration.ofMillis(metadata.getDuration())));
		context.setVariable("mediaPosterResourceName", posterData.isPresent() ? mediaPosterResourceName : null);
		context.setVariable("mediaAudios", audioLanguages);
		context.setVariable("mediaSubtitles", subtitleLanguages);
		context.setVariable("mediaResolutions", resolutions);
		context.setVariable("mediaBitrates", bitrates);
		context.setVariable("suggestAddRequirementId", suggestAddRequirementId);
		context.setVariable("metadataProvidersInfo", mediaMetadataContext.getMetadataProviderInfo());
		
		sendMail(notification, message -> {
			message.setSubject(messageSource.getMessage("mail.media.added.subject", new Object[0], locale));
			message.setText(templateEngine.process("mail/media-added.html", context), true);
			if(posterData.isPresent()){
				message.addInline(mediaPosterResourceName, new ByteArrayResource(posterData.get(), "Media poster"), "image/jpeg");
			}
		});
	}
	
	private void notifySimple(@NotNull NotificationEntity notification, @NotNull UserGroupEntity userGroupEntity, @NotNull MediaEntity media, @NotNull String subjectKey) throws MessagingException, UnsupportedEncodingException{
		var locale = userGroupEntity.getLocaleAsObject();
		var context = new Context();
		context.setLocale(userGroupEntity.getLocaleAsObject());
		
		var overseerrLogoData = getOverseerrLogoBytes();
		var plexLogoData = getPlexLogoBytes();
		var tmdbLogoData = getTmdbLogoBytes();
		var tvdbLogoData = getTvdbLogoBytes();
		var traktLogoData = getTraktLogoBytes();
		
		var overseerrLogoResourceName = "overseerrLogoResourceName";
		var plexLogoResourceName = "plexLogoResourceName";
		var tmdbLogoResourceName = "tmdbLogoResourceName";
		var tvdbLogoResourceName = "tvdbLogoResourceName";
		var traktLogoResourceName = "traktLogoResourceName";
		
		context.setVariable("service", this);
		context.setVariable("media", media);
		context.setVariable("thymeleafService", thymeleafService);
		context.setVariable("userGroup", userGroupEntity);
		context.setVariable("overseerrLogoResourceName", overseerrLogoData.isPresent() ? overseerrLogoResourceName : null);
		context.setVariable("plexLogoResourceName", plexLogoData.isPresent() ? plexLogoResourceName : null);
		context.setVariable("tmdbLogoResourceName", tmdbLogoData.isPresent() ? tmdbLogoResourceName : null);
		context.setVariable("tvdbLogoResourceName", tvdbLogoData.isPresent() ? tvdbLogoResourceName : null);
		context.setVariable("traktLogoResourceName", traktLogoData.isPresent() ? traktLogoResourceName : null);
		
		sendMail(notification, message -> {
			message.setSubject(messageSource.getMessage(subjectKey, new Object[0], locale));
			message.setText(templateEngine.process("mail/single-media.html", context), true);
			if(overseerrLogoData.isPresent()){
				message.addInline(overseerrLogoResourceName, new ByteArrayResource(overseerrLogoData.get(), "Overseerr logo"), "image/png");
			}
			if(plexLogoData.isPresent()){
				message.addInline(plexLogoResourceName, new ByteArrayResource(plexLogoData.get(), "Plex logo"), "image/png");
			}
			if(tmdbLogoData.isPresent()){
				message.addInline(tmdbLogoResourceName, new ByteArrayResource(tmdbLogoData.get(), "Tmdb logo"), "image/png");
			}
			if(tvdbLogoData.isPresent()){
				message.addInline(tvdbLogoResourceName, new ByteArrayResource(tvdbLogoData.get(), "Tvdb logo"), "image/png");
			}
			if(traktLogoData.isPresent()){
				message.addInline(traktLogoResourceName, new ByteArrayResource(traktLogoData.get(), "Trakt logo"), "image/png");
			}
		});
	}
	
	private void sendMail(@NotNull NotificationEntity notification, @NotNull MessageFiller messageFiller) throws MessagingException, UnsupportedEncodingException{
		var mimeMessage = emailSender.createMimeMessage();
		var mailHelper = new MimeMessageHelper(mimeMessage, true, "utf-8");
		
		mailHelper.setFrom(mailConfiguration.getFromAddress(), mailConfiguration.getFromName());
		mailHelper.setTo(notification.getValue().split(","));
		if(Objects.nonNull(mailConfiguration.getBccAddresses()) && !mailConfiguration.getBccAddresses().isEmpty()){
			mailHelper.setBcc(mailConfiguration.getBccAddresses().toArray(new String[0]));
		}
		
		messageFiller.accept(mailHelper);
		
		emailSender.send(mimeMessage);
	}
	
	@NotNull
	private Optional<byte[]> getOverseerrLogoBytes(){
		return getResourceBytes("static/overseerr.png");
	}
	
	@NotNull
	private Optional<byte[]> getPlexLogoBytes(){
		return getResourceBytes("static/plex.png");
	}
	
	@NotNull
	private Optional<byte[]> getTmdbLogoBytes(){
		return getResourceBytes("static/tmdb.png");
	}
	
	@NotNull
	private Optional<byte[]> getTvdbLogoBytes(){
		return getResourceBytes("static/tvdb.png");
	}
	
	@NotNull
	private Optional<byte[]> getTraktLogoBytes(){
		return getResourceBytes("static/trakt.png");
	}
	
	@NotNull
	private Optional<byte[]> getResourceBytes(@NotNull String path){
		try{
			var classPathResource = new ClassPathResource(path);
			if(!classPathResource.exists()){
				log.warn("Failed to get resource {}, does not exists", path);
				return Optional.empty();
			}
			return Optional.of(classPathResource.getContentAsByteArray());
		}
		catch(Exception e){
			log.error("Failed to get resource {}", path, e);
			return Optional.empty();
		}
	}
	
	private interface MessageFiller{
		void accept(MimeMessageHelper mimeMessageHelper) throws MessagingException, UnsupportedEncodingException;
	}
}
