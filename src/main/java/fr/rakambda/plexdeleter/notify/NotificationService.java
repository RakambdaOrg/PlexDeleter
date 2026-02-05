package fr.rakambda.plexdeleter.notify;

import fr.rakambda.plexdeleter.api.RequestFailedException;
import fr.rakambda.plexdeleter.api.servarr.radarr.RadarrApiService;
import fr.rakambda.plexdeleter.api.servarr.sonarr.SonarrApiService;
import fr.rakambda.plexdeleter.api.tautulli.TautulliApiService;
import fr.rakambda.plexdeleter.api.tautulli.data.GetMetadataResponse;
import fr.rakambda.plexdeleter.api.tmdb.TmdbApiService;
import fr.rakambda.plexdeleter.api.tvdb.TvdbApiService;
import fr.rakambda.plexdeleter.notify.context.CompositeMediaMetadataContext;
import fr.rakambda.plexdeleter.notify.context.MediaMetadataContext;
import fr.rakambda.plexdeleter.notify.context.ServarrMediaMetadataContext;
import fr.rakambda.plexdeleter.notify.context.TautulliMediaMetadataContext;
import fr.rakambda.plexdeleter.notify.context.TmdbMediaMetadataContext;
import fr.rakambda.plexdeleter.notify.context.TraktMediaMetadataContext;
import fr.rakambda.plexdeleter.notify.context.TvdbMediaMetadataContext;
import fr.rakambda.plexdeleter.storage.entity.MediaEntity;
import fr.rakambda.plexdeleter.storage.entity.MediaRequirementEntity;
import fr.rakambda.plexdeleter.storage.entity.MediaRequirementStatus;
import fr.rakambda.plexdeleter.storage.entity.UserGroupEntity;
import fr.rakambda.plexdeleter.storage.repository.MediaRepository;
import fr.rakambda.plexdeleter.storage.repository.UserGroupRepository;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class NotificationService{
	private final MailNotificationService mailNotificationService;
	private final DiscordNotificationService discordNotificationService;
	private final UserGroupRepository userGroupRepository;
	private final TvdbApiService tvdbApiService;
	private final TautulliApiService tautulliApiService;
	private final TmdbApiService tmdbApiService;
	private final MediaRepository mediaRepository;
	private final RadarrApiService radarrApiService;
	private final SonarrApiService sonarrApiService;
	
	@Autowired
	public NotificationService(MailNotificationService mailNotificationService, DiscordNotificationService discordNotificationService, UserGroupRepository userGroupRepository, TvdbApiService tvdbApiService, TautulliApiService tautulliApiService, TmdbApiService tmdbApiService, MediaRepository mediaRepository, RadarrApiService radarrApiService, SonarrApiService sonarrApiService){
		this.mailNotificationService = mailNotificationService;
		this.discordNotificationService = discordNotificationService;
		this.userGroupRepository = userGroupRepository;
		this.tvdbApiService = tvdbApiService;
		this.tautulliApiService = tautulliApiService;
		this.tmdbApiService = tmdbApiService;
		this.mediaRepository = mediaRepository;
		this.radarrApiService = radarrApiService;
		this.sonarrApiService = sonarrApiService;
	}
	
	public void notifyWatchlist(@NonNull UserGroupEntity userGroupEntity, @NonNull Collection<MediaRequirementEntity> requirements) throws NotifyException{
		try{
			if(requirements.isEmpty()){
				return;
			}
			log.info("Notifying watchlist to {}", userGroupEntity);
			if(!userGroupEntity.getNotifyWatchlist()){
				return;
			}
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
	
	public void notifyRequirementAdded(@NonNull UserGroupEntity userGroupEntity, @NonNull MediaEntity media) throws NotifyException{
		try{
			log.info("Notifying a requirement on {} has been added to {}", media, userGroupEntity);
			if(!userGroupEntity.getNotifyRequirementAdded()){
				return;
			}
			var notification = userGroupEntity.getNotification();
			if(Objects.isNull(notification)){
				return;
			}
			switch(notification.getType()){
				case MAIL -> mailNotificationService.notifyRequirementAdded(notification, userGroupEntity, media, getMetadata(media));
				case DISCORD, DISCORD_THREAD -> discordNotificationService.notifyRequirementAdded(notification, userGroupEntity, media);
			}
		}
		catch(MessagingException | UnsupportedEncodingException | InterruptedException | RequestFailedException e){
			throw new NotifyException("Failed to notify media requirement added for group %s".formatted(userGroupEntity), e);
		}
	}
	
	@Nullable
	private MediaMetadataContext getMetadata(@NonNull MediaEntity media) throws RequestFailedException{
		if(Objects.isNull(media.getPlexId())){
			return null;
		}
		var metadata = tautulliApiService.getMetadata(media.getPlexId()).getResponse().getData();
		if(Objects.isNull(metadata)){
			return null;
		}
		return buildMediaMetadataContext(metadata);
	}
	
	@Transactional
	public void notifyMediaAvailable(@NonNull MediaEntity media) throws NotifyException{
		var userGroups = userGroupRepository.findAllByHasRequirementOnOverseerr(media.getId(), MediaRequirementStatus.WAITING);
		for(var userGroup : userGroups){
			notifyMediaAvailable(userGroup, media);
		}
	}
	
	public void notifyMediaAvailable(@NonNull UserGroupEntity userGroupEntity, @NonNull MediaEntity media) throws NotifyException{
		try{
			log.info("Notifying {} is available to {}", media, userGroupEntity);
			if(!userGroupEntity.getNotifyMediaAvailable()){
				return;
			}
			var notification = userGroupEntity.getNotification();
			if(Objects.isNull(notification)){
				return;
			}
			switch(notification.getType()){
				case MAIL -> mailNotificationService.notifyMediaAvailable(notification, userGroupEntity, media, getMetadata(media));
				case DISCORD, DISCORD_THREAD -> discordNotificationService.notifyMediaAvailable(notification, userGroupEntity, media);
			}
		}
		catch(MessagingException | UnsupportedEncodingException | InterruptedException | RequestFailedException e){
			throw new NotifyException("Failed to notify media added for group %s".formatted(userGroupEntity), e);
		}
	}
	
	public void notifyMediaWatched(@NonNull UserGroupEntity userGroupEntity, @NonNull MediaEntity media) throws NotifyException{
		try{
			log.info("Notifying {} has been watched to {}", media, userGroupEntity);
			if(!userGroupEntity.getNotifyMediaWatched()){
				return;
			}
			var notification = userGroupEntity.getNotification();
			if(Objects.isNull(notification)){
				return;
			}
			switch(notification.getType()){
				case MAIL -> mailNotificationService.notifyMediaWatched(notification, userGroupEntity, media, getMetadata(media));
				case DISCORD, DISCORD_THREAD -> discordNotificationService.notifyMediaWatched(notification, userGroupEntity, media);
			}
		}
		catch(MessagingException | UnsupportedEncodingException | InterruptedException | RequestFailedException e){
			throw new NotifyException("Failed to notify media watched for group %s".formatted(userGroupEntity), e);
		}
	}
	
	public void notifyMediaDeleted(@NonNull UserGroupEntity userGroupEntity, @NonNull MediaEntity media) throws NotifyException{
		try{
			log.info("Notifying {} has been deleted to {}", media, userGroupEntity);
			if(!userGroupEntity.getNotifyMediaDeleted()){
				return;
			}
			var notification = userGroupEntity.getNotification();
			if(Objects.isNull(notification)){
				return;
			}
			switch(notification.getType()){
				case MAIL -> mailNotificationService.notifyMediaDeleted(notification, userGroupEntity, media, getMetadata(media));
				case DISCORD, DISCORD_THREAD -> discordNotificationService.notifyMediaDeleted(notification, userGroupEntity, media);
			}
		}
		catch(MessagingException | UnsupportedEncodingException | InterruptedException | RequestFailedException e){
			throw new NotifyException("Failed to notify media deleted for group %s".formatted(userGroupEntity), e);
		}
	}
	
	public void notifyRequirementManuallyWatched(@NonNull UserGroupEntity userGroupEntity, @NonNull MediaEntity media) throws NotifyException{
		try{
			log.info("Notifying {} has been manually marked as watched to {}", media, userGroupEntity);
			if(!userGroupEntity.getNotifyRequirementManuallyWatched()){
				return;
			}
			var notification = userGroupEntity.getNotification();
			if(Objects.isNull(notification)){
				return;
			}
			switch(notification.getType()){
				case MAIL -> mailNotificationService.notifyRequirementManuallyWatched(notification, userGroupEntity, media, getMetadata(media));
				case DISCORD, DISCORD_THREAD -> discordNotificationService.notifyRequirementManuallyWatched(notification, userGroupEntity, media);
			}
		}
		catch(MessagingException | UnsupportedEncodingException | InterruptedException | RequestFailedException e){
			throw new NotifyException("Failed to notify media requirement manually marked as watched for group %s".formatted(userGroupEntity), e);
		}
	}
	
	public void notifyRequirementManuallyAbandoned(@NonNull UserGroupEntity userGroupEntity, @NonNull MediaEntity media) throws NotifyException{
		try{
			log.info("Notifying {} has been manually marked as abandoned to {}", media, userGroupEntity);
			if(!userGroupEntity.getNotifyRequirementManuallyAbandoned()){
				return;
			}
			var notification = userGroupEntity.getNotification();
			if(Objects.isNull(notification)){
				return;
			}
			switch(notification.getType()){
				case MAIL -> mailNotificationService.notifyRequirementManuallyAbandoned(notification, userGroupEntity, media, getMetadata(media));
				case DISCORD, DISCORD_THREAD -> discordNotificationService.notifyRequirementManuallyAbandoned(notification, userGroupEntity, media);
			}
		}
		catch(MessagingException | UnsupportedEncodingException | InterruptedException | RequestFailedException e){
			throw new NotifyException("Failed to notify media requirement manually marked as abandoned for group %s".formatted(userGroupEntity), e);
		}
	}
	
	public void notifyMediaAdded(@NonNull GetMetadataResponse metadata) throws NotifyException{
		var ratingKey = switch(metadata.getMediaType()){
			case MOVIE, SEASON -> metadata.getRatingKey();
			case EPISODE -> metadata.getParentRatingKey();
			case TRACK, ARTIST, SHOW, PHOTO -> null;
		};
		var mediaIndex = switch(metadata.getMediaType()){
			case MOVIE -> 1;
			case SEASON -> metadata.getMediaIndex();
			case EPISODE -> metadata.getParentMediaIndex();
			case TRACK, ARTIST, SHOW, PHOTO -> 1;
		};
		
		if(Objects.isNull(ratingKey)){
			log.warn("Not notifying media file added, could not determine rating key from {}", metadata);
			return;
		}
		
		var media = mediaRepository.findByPlexId(ratingKey).orElse(null);
		var mediaMetadataContext = buildMediaMetadataContext(metadata);
		
		var tmdbId = mediaMetadataContext.find(TmdbMediaMetadataContext.class).flatMap(TmdbMediaMetadataContext::getTmdbId);
		var tvdbId = mediaMetadataContext.find(TvdbMediaMetadataContext.class).flatMap(TvdbMediaMetadataContext::getTvdbId);
		
		if(Objects.isNull(media)){
			var mediaOther = tmdbId.flatMap(id -> mediaRepository.findByTmdbIdAndIndex(id, mediaIndex))
					.or(() -> tvdbId.flatMap(id -> mediaRepository.findByTvdbIdAndIndex(id, mediaIndex)));
			
			var ratingKeyToSet = switch(metadata.getMediaType()){
				case MOVIE, SEASON -> metadata.getRatingKey();
				case EPISODE -> metadata.getParentRatingKey();
				case TRACK, ARTIST, SHOW, PHOTO -> null;
			};
			
			if(mediaOther.isPresent() && Objects.nonNull(ratingKeyToSet)){
				mediaOther.get().setPlexId(ratingKeyToSet);
				media = mediaRepository.save(mediaOther.get());
			}
		}
		
		var userGroupsRequirement = userGroupRepository.findAllByHasRequirementOnPlex(
				ratingKey,
				MediaRequirementStatus.WAITING,
				metadata.getLibraryName(),
				tmdbId.orElse(null),
				tvdbId.orElse(null),
				mediaIndex
		);
		
		for(var userGroup : userGroupsRequirement){
			notifyMediaAdded(userGroup, mediaMetadataContext, media);
		}
	}
	
	@NonNull
	private CompositeMediaMetadataContext buildMediaMetadataContext(@NonNull GetMetadataResponse metadata){
		var tautulliMediaMetadataContext = new TautulliMediaMetadataContext(metadata, tautulliApiService);
		var tmdbMediaMetadataContext = new TmdbMediaMetadataContext(metadata, tmdbApiService);
		var tvdbMediaMetadataContext = new TvdbMediaMetadataContext(metadata, tvdbApiService);
		var traktMediaMetadataContext = new TraktMediaMetadataContext(metadata);
		var servarrMediaMetadataContext = new ServarrMediaMetadataContext(metadata, radarrApiService, sonarrApiService);
		return new CompositeMediaMetadataContext(metadata, List.of(
				tautulliMediaMetadataContext,
				tmdbMediaMetadataContext,
				tvdbMediaMetadataContext,
				traktMediaMetadataContext,
				servarrMediaMetadataContext
		));
	}
	
	private void notifyMediaAdded(@NonNull UserGroupEntity userGroupEntity, @NonNull MediaMetadataContext metadata, @Nullable MediaEntity media) throws NotifyException{
		try{
			log.info("Notifying {} has been added to {}", metadata.getMetadata(), userGroupEntity);
			if(!userGroupEntity.getNotifyMediaAdded()){
				return;
			}
			var notification = userGroupEntity.getNotificationMediaAdded();
			if(Objects.isNull(notification)){
				return;
			}
			switch(notification.getType()){
				case MAIL -> mailNotificationService.notifyMediaAdded(notification, userGroupEntity, media, metadata);
				case DISCORD, DISCORD_THREAD -> discordNotificationService.notifyMediaAdded(notification, userGroupEntity, metadata, media);
			}
		}
		catch(MessagingException | UnsupportedEncodingException | RequestFailedException | InterruptedException e){
			throw new NotifyException("Failed to notify media added for group %s".formatted(userGroupEntity), e);
		}
	}
}
