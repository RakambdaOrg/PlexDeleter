package fr.rakambda.plexdeleter.notify;

import fr.rakambda.plexdeleter.api.RequestFailedException;
import fr.rakambda.plexdeleter.api.tautulli.TautulliApiService;
import fr.rakambda.plexdeleter.api.tautulli.data.GetMetadataResponse;
import fr.rakambda.plexdeleter.api.tmdb.TmdbService;
import fr.rakambda.plexdeleter.api.tvdb.TvdbService;
import fr.rakambda.plexdeleter.notify.context.CompositeMediaMetadataContext;
import fr.rakambda.plexdeleter.notify.context.MediaMetadataContext;
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
import java.util.Optional;

@Slf4j
@Service
public class NotificationService{
	private final MailNotificationService mailNotificationService;
	private final DiscordNotificationService discordNotificationService;
	private final UserGroupRepository userGroupRepository;
	private final TvdbService tvdbService;
	private final TautulliApiService tautulliApiService;
	private final TmdbService tmdbService;
	private final MediaRepository mediaRepository;
	
	@Autowired
	public NotificationService(MailNotificationService mailNotificationService, DiscordNotificationService discordNotificationService, UserGroupRepository userGroupRepository, TvdbService tvdbService, TautulliApiService tautulliApiService, TmdbService tmdbService, MediaRepository mediaRepository){
		this.mailNotificationService = mailNotificationService;
		this.discordNotificationService = discordNotificationService;
		this.userGroupRepository = userGroupRepository;
		this.tvdbService = tvdbService;
		this.tautulliApiService = tautulliApiService;
		this.tmdbService = tmdbService;
		this.mediaRepository = mediaRepository;
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
				case MAIL -> mailNotificationService.notifyRequirementAdded(notification, userGroupEntity, media);
				case DISCORD, DISCORD_THREAD -> discordNotificationService.notifyRequirementAdded(notification, userGroupEntity, media);
			}
		}
		catch(MessagingException | UnsupportedEncodingException | InterruptedException | RequestFailedException e){
			throw new NotifyException("Failed to notify media requirement added for group %s".formatted(userGroupEntity), e);
		}
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
				case MAIL -> mailNotificationService.notifyMediaAvailable(notification, userGroupEntity, media);
				case DISCORD, DISCORD_THREAD -> discordNotificationService.notifyMediaAvailable(notification, userGroupEntity, media);
			}
		}
		catch(MessagingException | UnsupportedEncodingException | InterruptedException | RequestFailedException e){
			throw new NotifyException("Failed to notify media added for group %s".formatted(userGroupEntity), e);
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
				case MAIL -> mailNotificationService.notifyMediaDeleted(notification, userGroupEntity, media);
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
				case MAIL -> mailNotificationService.notifyRequirementManuallyWatched(notification, userGroupEntity, media);
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
				case MAIL -> mailNotificationService.notifyRequirementManuallyAbandoned(notification, userGroupEntity, media);
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
		
		var tmdbMediaMetadataContext = new TmdbMediaMetadataContext(tautulliApiService, metadata, tmdbService);
		var tvdbMediaMetadataContext = new TvdbMediaMetadataContext(tautulliApiService, metadata, tvdbService);
		var traktMediaMetadataContext = new TraktMediaMetadataContext(tautulliApiService, metadata);
		var mediaMetadataContext = new CompositeMediaMetadataContext(tautulliApiService, metadata, List.of(
				tmdbMediaMetadataContext,
				tvdbMediaMetadataContext,
				traktMediaMetadataContext
		));
		
		var media = mediaRepository.findByPlexId(ratingKey).orElse(null);
		
		if(Objects.isNull(media)){
			var mediaOther = tmdbMediaMetadataContext.getTmdbId().flatMap(id -> mediaRepository.findByTmdbIdAndIndex(id, mediaIndex))
					.or(() -> tvdbMediaMetadataContext.getTvdbId().flatMap(id -> mediaRepository.findByTvdbIdAndIndex(id, mediaIndex)));
			
			var ratingKeyToSet = switch(metadata.getMediaType()){
				case MOVIE, SEASON -> metadata.getRatingKey();
				case EPISODE -> metadata.getParentRatingKey();
				case TRACK, ARTIST, SHOW, PHOTO -> null;
			};
			
			var rootRatingKeyToSet = Optional.ofNullable(metadata.getGrandparentRatingKey())
					.or(() -> Optional.ofNullable(metadata.getParentRatingKey()))
					.orElseGet(metadata::getRatingKey);
			
			if(mediaOther.isPresent() && Objects.nonNull(ratingKeyToSet)){
				mediaOther.get().setPlexId(ratingKeyToSet);
				mediaOther.get().setRootPlexId(rootRatingKeyToSet);
				media = mediaRepository.save(mediaOther.get());
			}
		}
		
		var userGroupsRequirement = userGroupRepository.findAllByHasRequirementOnPlex(
				ratingKey,
				MediaRequirementStatus.WAITING,
				metadata.getLibraryName(),
				tmdbMediaMetadataContext.getTmdbId().orElse(null),
				tvdbMediaMetadataContext.getTvdbId().orElse(null),
				mediaIndex
		);
		
		for(var userGroup : userGroupsRequirement){
			notifyMediaAdded(userGroup, mediaMetadataContext, media);
		}
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
