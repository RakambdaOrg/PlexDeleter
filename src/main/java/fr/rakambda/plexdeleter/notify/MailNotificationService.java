package fr.rakambda.plexdeleter.notify;

import ch.digitalfondue.mjml4j.Mjml4j;
import com.googlecode.htmlcompressor.compressor.HtmlCompressor;
import fr.rakambda.plexdeleter.api.tautulli.data.AudioMediaPartStream;
import fr.rakambda.plexdeleter.api.tautulli.data.MediaInfo;
import fr.rakambda.plexdeleter.api.tautulli.data.SubtitlesMediaPartStream;
import fr.rakambda.plexdeleter.config.ApplicationConfiguration;
import fr.rakambda.plexdeleter.config.MailConfiguration;
import fr.rakambda.plexdeleter.notify.context.MediaMetadataContext;
import fr.rakambda.plexdeleter.service.LanguageFlagService;
import fr.rakambda.plexdeleter.service.ThymeleafService;
import fr.rakambda.plexdeleter.service.WatchService;
import fr.rakambda.plexdeleter.storage.entity.MediaEntity;
import fr.rakambda.plexdeleter.storage.entity.MediaRequirementEntity;
import fr.rakambda.plexdeleter.storage.entity.NotificationEntity;
import fr.rakambda.plexdeleter.storage.entity.UserGroupEntity;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import static fr.rakambda.plexdeleter.storage.entity.MediaEntity.COMPARATOR_BY_TYPE_THEN_NAME_THEN_INDEX;

@Slf4j
@Service
public class MailNotificationService extends AbstractNotificationService{
	private final JavaMailSender emailSender;
	private final MailConfiguration mailConfiguration;
	private final MessageSource messageSource;
	private final SpringTemplateEngine templateEngine;
	private final ThymeleafService thymeleafService;
	private final LanguageFlagService languageFlagService;
	private final HtmlCompressor htmlCompressor;
	
	@Autowired
	public MailNotificationService(JavaMailSender emailSender, ApplicationConfiguration applicationConfiguration, MessageSource messageSource, WatchService watchService, SpringTemplateEngine templateEngine, ThymeleafService thymeleafService, LanguageFlagService languageFlagService){
		super(watchService, messageSource);
		this.emailSender = emailSender;
		this.messageSource = messageSource;
		mailConfiguration = applicationConfiguration.getMail();
		this.templateEngine = templateEngine;
		this.thymeleafService = thymeleafService;
		this.languageFlagService = languageFlagService;
		
		htmlCompressor = new HtmlCompressor();
		htmlCompressor.setRemoveIntertagSpaces(true);
		htmlCompressor.setRemoveQuotes(false);
		htmlCompressor.setCompressCss(false);
	}
	
	public void notifyWatchlist(@NonNull NotificationEntity notification, @NonNull UserGroupEntity userGroupEntity, @NonNull Collection<MediaRequirementEntity> requirements) throws MessagingException, UnsupportedEncodingException{
		var locale = userGroupEntity.getLocaleAsObject();
		var context = new Context();
		context.setLocale(userGroupEntity.getLocaleAsObject());
		
		var availableMedia = requirements.stream()
				.map(MediaRequirementEntity::getMedia)
				.filter(m -> m.getStatus().isFullyDownloaded())
				.sorted(COMPARATOR_BY_TYPE_THEN_NAME_THEN_INDEX)
				.toList();
		var downloadingMedia = requirements.stream()
				.map(MediaRequirementEntity::getMedia)
				.filter(m -> m.getStatus().isDownloadStarted() && !m.getStatus().isFullyDownloaded())
				.sorted(COMPARATOR_BY_TYPE_THEN_NAME_THEN_INDEX)
				.toList();
		var notYetAvailableMedia = requirements.stream()
				.map(MediaRequirementEntity::getMedia)
				.filter(m -> !m.getStatus().isDownloadStarted())
				.sorted(COMPARATOR_BY_TYPE_THEN_NAME_THEN_INDEX)
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
		
		sendMail(notification, context, "mail.watchlist.subject", locale, "mail/watchlist.html", message -> {}, availableMedia, downloadingMedia, notYetAvailableMedia);
	}
	
	public void notifyRequirementAdded(@NonNull NotificationEntity notification, @NonNull UserGroupEntity userGroupEntity, @NonNull MediaEntity media) throws MessagingException, UnsupportedEncodingException{
		notifySimple(notification, userGroupEntity, media, "mail.requirement.added.subject");
	}
	
	public void notifyMediaAvailable(@NonNull NotificationEntity notification, @NonNull UserGroupEntity userGroupEntity, @NonNull MediaEntity media) throws MessagingException, UnsupportedEncodingException{
		notifySimple(notification, userGroupEntity, media, "mail.media.available.subject");
	}
	
	public void notifyMediaDeleted(@NonNull NotificationEntity notification, @NonNull UserGroupEntity userGroupEntity, @NonNull MediaEntity media) throws MessagingException, UnsupportedEncodingException{
		notifySimple(notification, userGroupEntity, media, "mail.media.deleted.subject");
	}
	
	public void notifyRequirementManuallyWatched(@NonNull NotificationEntity notification, @NonNull UserGroupEntity userGroupEntity, @NonNull MediaEntity media) throws MessagingException, UnsupportedEncodingException{
		notifySimple(notification, userGroupEntity, media, "mail.requirement.manually-watched.subject");
	}
	
	public void notifyRequirementManuallyAbandoned(@NonNull NotificationEntity notification, @NonNull UserGroupEntity userGroupEntity, @NonNull MediaEntity media) throws MessagingException, UnsupportedEncodingException{
		notifySimple(notification, userGroupEntity, media, "mail.requirement.manually-abandoned.subject");
	}
	
	public void notifyMediaAdded(@NonNull NotificationEntity notification, @NonNull UserGroupEntity userGroupEntity, @Nullable MediaEntity media, @NonNull MediaMetadataContext mediaMetadataContext) throws MessagingException, UnsupportedEncodingException{
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
				.filter(Objects::nonNull)
				.distinct()
				.map(s -> Objects.equals(s, "") ? "unknown" : s)
				.sorted()
				.map(s -> new LanguageInfo("locale.%s".formatted(s), languageFlagService.getFlagUrl(s)))
				.toList();
		var subtitleLanguages = getMediaStreams(metadata, SubtitlesMediaPartStream.class)
				.map(SubtitlesMediaPartStream::getSubtitleLanguageCode)
				.filter(Objects::nonNull)
				.distinct()
				.map(s -> Objects.equals(s, "") ? "unknown" : s)
				.sorted()
				.map(s -> new LanguageInfo("locale.%s".formatted(s), languageFlagService.getFlagUrl(s)))
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
		context.setVariable("mediaActors", metadata.getActors().stream().limit(20).toList());
		context.setVariable("mediaGenres", mediaMetadataContext.getGenres(messageSource, locale).orElseGet(metadata::getGenres));
		context.setVariable("mediaDuration", getMediaDuration(Duration.ofMillis(metadata.getDuration())));
		context.setVariable("mediaPosterResourceName", posterData.isPresent() ? mediaPosterResourceName : null);
		context.setVariable("mediaAudios", audioLanguages);
		context.setVariable("mediaSubtitles", subtitleLanguages);
		context.setVariable("mediaResolutions", resolutions);
		context.setVariable("mediaBitrates", bitrates);
		context.setVariable("suggestAddRequirementId", suggestAddRequirementId);
		context.setVariable("metadataProvidersInfo", mediaMetadataContext.getMetadataProviderInfo());
		
		sendMail(notification, context, "mail.media.added.subject", locale, "mail/media-added.html", message -> {
			if(posterData.isPresent()){
				message.addInline(mediaPosterResourceName, new ByteArrayResource(posterData.get()), "image/jpeg");
			}
		}, List.of());
	}
	
	private void notifySimple(@NonNull NotificationEntity notification, @NonNull UserGroupEntity userGroupEntity, @NonNull MediaEntity media, @NonNull String subjectKey) throws MessagingException, UnsupportedEncodingException{
		var locale = userGroupEntity.getLocaleAsObject();
		var context = new Context();
		context.setLocale(userGroupEntity.getLocaleAsObject());
		
		context.setVariable("service", this);
		context.setVariable("medias", List.of(media));
		context.setVariable("thymeleafService", thymeleafService);
		context.setVariable("userGroup", userGroupEntity);
		
		sendMail(notification, context, subjectKey, locale, "mail/single-media.html", message -> {}, List.of(media));
	}
	
	@SafeVarargs
	private void sendMail(
			@NonNull NotificationEntity notification,
			@NonNull Context context,
			@NonNull String subjectKey,
			@NonNull Locale locale,
			@NonNull String template,
			@NonNull MessageFiller messageFiller,
			@NonNull List<MediaEntity>... mediasForResources
	) throws MessagingException, UnsupportedEncodingException{
		var mimeMessage = emailSender.createMimeMessage();
		var mailHelper = new MimeMessageHelper(mimeMessage, true, "utf-8");
		
		mailHelper.setFrom(mailConfiguration.getFromAddress(), mailConfiguration.getFromName());
		mailHelper.setTo(notification.getValue().split(","));
		if(Objects.nonNull(mailConfiguration.getBccAddresses()) && !mailConfiguration.getBccAddresses().isEmpty()){
			mailHelper.setBcc(mailConfiguration.getBccAddresses().toArray(new String[0]));
		}
		
		var overseerrLogoResourceName = "overseerrLogoResourceName";
		var plexLogoResourceName = "plexLogoResourceName";
		var tmdbLogoResourceName = "tmdbLogoResourceName";
		var tvdbLogoResourceName = "tvdbLogoResourceName";
		var traktLogoResourceName = "traktLogoResourceName";
		
		var hasOverseerrLink = hasAnyMediaValueNotNull(MediaEntity::getOverseerrId, mediasForResources);
		var hasPlexLink = hasAnyMediaValueNotNull(MediaEntity::getPlexId, mediasForResources);
		var hasTmdbLink = hasAnyMediaValueNotNull(MediaEntity::getTmdbId, mediasForResources);
		var hasTvdbLink = hasAnyMediaValueNotNull(MediaEntity::getTvdbId, mediasForResources);
		var hasTraktLink = hasAnyMediaValueNotNull(MediaEntity::getTmdbId, mediasForResources);
		
		var overseerrLogoData = hasOverseerrLink ? getOverseerrLogoBytes() : Optional.<byte[]> empty();
		var plexLogoData = hasPlexLink ? getPlexLogoBytes() : Optional.<byte[]> empty();
		var tmdbLogoData = hasTmdbLink ? getTmdbLogoBytes() : Optional.<byte[]> empty();
		var tvdbLogoData = hasTvdbLink ? getTvdbLogoBytes() : Optional.<byte[]> empty();
		var traktLogoData = hasTraktLink ? getTraktLogoBytes() : Optional.<byte[]> empty();
		
		context.setVariable("overseerrLogoResourceName", overseerrLogoData.isPresent() ? overseerrLogoResourceName : null);
		context.setVariable("plexLogoResourceName", plexLogoData.isPresent() ? plexLogoResourceName : null);
		context.setVariable("tmdbLogoResourceName", tmdbLogoData.isPresent() ? tmdbLogoResourceName : null);
		context.setVariable("tvdbLogoResourceName", tvdbLogoData.isPresent() ? tvdbLogoResourceName : null);
		context.setVariable("traktLogoResourceName", traktLogoData.isPresent() ? traktLogoResourceName : null);
		
		mailHelper.setSubject(messageSource.getMessage(subjectKey, new Object[0], locale));
		mailHelper.setText(renderMail(templateEngine.process(template, context), locale), true);
		
		if(overseerrLogoData.isPresent()){
			mailHelper.addInline(overseerrLogoResourceName, new ByteArrayResource(overseerrLogoData.get()), "image/png");
		}
		if(plexLogoData.isPresent()){
			mailHelper.addInline(plexLogoResourceName, new ByteArrayResource(plexLogoData.get()), "image/png");
		}
		if(tmdbLogoData.isPresent()){
			mailHelper.addInline(tmdbLogoResourceName, new ByteArrayResource(tmdbLogoData.get()), "image/png");
		}
		if(tvdbLogoData.isPresent()){
			mailHelper.addInline(tvdbLogoResourceName, new ByteArrayResource(tvdbLogoData.get()), "image/png");
		}
		if(traktLogoData.isPresent()){
			mailHelper.addInline(traktLogoResourceName, new ByteArrayResource(traktLogoData.get()), "image/png");
		}
		
		messageFiller.accept(mailHelper);
		
		emailSender.send(mimeMessage);
	}
	
	private String renderMail(String mjml, Locale locale){
		var configuration = new Mjml4j.Configuration(locale.getLanguage());
		var rendered = Mjml4j.render(mjml, configuration);
		return htmlCompressor.compress(rendered);
	}
	
	@NonNull
	private Optional<byte[]> getOverseerrLogoBytes(){
		return getResourceBytes("static/overseerr.png");
	}
	
	@NonNull
	private Optional<byte[]> getPlexLogoBytes(){
		return getResourceBytes("static/plex.png");
	}
	
	@NonNull
	private Optional<byte[]> getTmdbLogoBytes(){
		return getResourceBytes("static/tmdb.png");
	}
	
	@NonNull
	private Optional<byte[]> getTvdbLogoBytes(){
		return getResourceBytes("static/tvdb.png");
	}
	
	@NonNull
	private Optional<byte[]> getTraktLogoBytes(){
		return getResourceBytes("static/trakt.png");
	}
	
	@NonNull
	private Optional<byte[]> getResourceBytes(@NonNull String path){
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
	
	@SafeVarargs
	private boolean hasAnyMediaValueNotNull(Function<MediaEntity, Object> propertyExtractor, List<MediaEntity>... medias){
		return Arrays.stream(medias)
				.flatMap(Collection::stream)
				.map(propertyExtractor)
				.anyMatch(Objects::nonNull);
	}
	
	private interface MessageFiller{
		void accept(MimeMessageHelper mimeMessageHelper) throws MessagingException, UnsupportedEncodingException;
	}
}
