package fr.rakambda.plexdeleter.service;

import fr.rakambda.plexdeleter.api.RequestFailedException;
import fr.rakambda.plexdeleter.api.overseerr.OverseerrApiService;
import fr.rakambda.plexdeleter.api.overseerr.data.MediaInfo;
import fr.rakambda.plexdeleter.api.overseerr.data.MovieMedia;
import fr.rakambda.plexdeleter.api.overseerr.data.SeriesMedia;
import fr.rakambda.plexdeleter.api.plex.rest.PlexMediaServerApiService;
import fr.rakambda.plexdeleter.api.plex.rest.data.Label;
import fr.rakambda.plexdeleter.api.plex.rest.data.Metadata;
import fr.rakambda.plexdeleter.api.servarr.radarr.RadarrApiService;
import fr.rakambda.plexdeleter.api.servarr.sonarr.SonarrApiService;
import fr.rakambda.plexdeleter.api.servarr.sonarr.data.Season;
import fr.rakambda.plexdeleter.api.servarr.sonarr.data.Statistics;
import fr.rakambda.plexdeleter.api.tautulli.TautulliApiService;
import fr.rakambda.plexdeleter.messaging.SupervisionService;
import fr.rakambda.plexdeleter.notify.NotificationService;
import fr.rakambda.plexdeleter.notify.NotifyException;
import fr.rakambda.plexdeleter.service.data.DeleteMediaResponse;
import fr.rakambda.plexdeleter.storage.entity.MediaEntity;
import fr.rakambda.plexdeleter.storage.entity.MediaRequirementEntity;
import fr.rakambda.plexdeleter.storage.entity.MediaRequirementStatus;
import fr.rakambda.plexdeleter.storage.entity.MediaStatus;
import fr.rakambda.plexdeleter.storage.entity.MediaType;
import fr.rakambda.plexdeleter.storage.entity.UserGroupEntity;
import fr.rakambda.plexdeleter.storage.entity.UserPersonEntity;
import fr.rakambda.plexdeleter.storage.repository.MediaRepository;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import java.time.Instant;
import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import static fr.rakambda.plexdeleter.storage.entity.MediaRequirementStatus.WAITING;

@Slf4j
@Service
public class MediaService{
	private final TautulliApiService tautulliApiService;
	private final SupervisionService supervisionService;
	private final MediaRepository mediaRepository;
	private final OverseerrApiService overseerrApiService;
	private final SonarrApiService sonarrApiService;
	private final RadarrApiService radarrApiService;
	private final NotificationService notificationService;
	private final PlexMediaServerApiService plexMediaServerApiService;
	private final Lock mediaOperationLock;
	
	@Autowired
	public MediaService(TautulliApiService tautulliApiService, SupervisionService supervisionService, MediaRepository mediaRepository, OverseerrApiService overseerrApiService, SonarrApiService sonarrApiService, RadarrApiService radarrApiService, NotificationService notificationService, PlexMediaServerApiService plexMediaServerApiService){
		this.tautulliApiService = tautulliApiService;
		this.supervisionService = supervisionService;
		this.mediaRepository = mediaRepository;
		this.overseerrApiService = overseerrApiService;
		this.sonarrApiService = sonarrApiService;
		this.radarrApiService = radarrApiService;
		this.notificationService = notificationService;
		this.plexMediaServerApiService = plexMediaServerApiService;
		this.mediaOperationLock = new ReentrantLock();
	}
	
	public void updateAll(){
		log.info("Updating medias");
		
		var medias = mediaRepository.findAllByStatusIn(MediaStatus.allNeedRefresh());
		for(var media : medias){
			try{
				update(media);
			}
			catch(UpdateException | RequestFailedException | NotifyException e){
				log.error("Failed to update media {}", media, e);
			}
		}
		
		log.info("Done updating {} media", medias.size());
	}
	
	public void updateAllMetadata(){
		log.info("Updating medias metadata");
		
		var medias = mediaRepository.findAll();
		for(var media : medias){
			try{
				update(media);
			}
			catch(UpdateException | RequestFailedException | NotifyException e){
				log.error("Failed to update media {}", media, e);
			}
		}
		
		log.info("Done updating {} media metadata", medias.size());
	}
	
	@NonNull
	public MediaEntity update(@NonNull MediaEntity mediaEntity) throws UpdateException, RequestFailedException, NotifyException{
		return update(mediaEntity, false);
	}
	
	@NonNull
	public MediaEntity update(@NonNull MediaEntity mediaEntity, boolean forceMediaCount) throws UpdateException, RequestFailedException, NotifyException{
		mediaOperationLock.lock();
		try{
			log.info("Updating media {}", mediaEntity);
			
			updateFromOverseerr(mediaEntity);
			updateFromTautulli(mediaEntity);
			updateFromServarr(mediaEntity, forceMediaCount);
			
			if(mediaEntity.getStatus().isNeedsMetadataRefresh()){
				if(mediaEntity.getPartsCount() <= 0){
					log.warn("Failed to update {}, not enough info", mediaEntity);
					supervisionService.send("\uD83D\uDEAB Could not update %s", mediaEntity);
				}
				else if(mediaEntity.getAvailablePartsCount() >= mediaEntity.getPartsCount()){
					if(!mediaEntity.getStatus().isFullyDownloaded()){
						notificationService.notifyMediaAvailable(mediaEntity);
					}
					var newAvailability = Objects.nonNull(mediaEntity.getPlexId()) ? MediaStatus.DOWNLOADED : MediaStatus.DOWNLOADED_NEED_METADATA;
					if(!Objects.equals(newAvailability, mediaEntity.getStatus())){
						mediaEntity.setStatus(newAvailability);
						log.info("Marked media {} as {}", mediaEntity, mediaEntity.getStatus());
						supervisionService.send("\uD83C\uDD97 Marked %d as %s: %s (%d/%d)", mediaEntity.getId(), mediaEntity.getStatus(), mediaEntity, mediaEntity.getPartsCount(), mediaEntity.getAvailablePartsCount());
					}
				}
				else if(mediaEntity.getAvailablePartsCount() > 0){
					mediaEntity.setStatus(MediaStatus.DOWNLOADING);
				}
			}
			
			if(mediaEntity.getStatus().isNeedsRequirementsRefresh()){
				handleRequirements(mediaEntity);
			}
			
			return mediaRepository.save(mediaEntity);
		}
		finally{
			mediaOperationLock.unlock();
		}
	}
	
	private void handleRequirements(@NonNull MediaEntity mediaEntity){
		if(!mediaEntity.getStatus().isFullyDownloaded() || mediaEntity.getStatus().isNeedsMetadataRefresh()){
			return;
		}
		if(Objects.isNull(mediaEntity.getRequirements()) || mediaEntity.getRequirements().isEmpty()){
			log.warn("Downloaded media {} has no requirements", mediaEntity);
			supervisionService.send("\uD83D\uDEAB Downloaded media %d has no requirements", mediaEntity.getId());
			return;
		}
		
		if(mediaEntity.getRequirements().stream()
				.map(MediaRequirementEntity::getStatus)
				.allMatch(MediaRequirementStatus::isCompleted)){
			mediaEntity.setStatus(MediaStatus.PENDING_DELETION);
			log.info("Marked media {} as {}", mediaEntity, mediaEntity.getStatus());
			supervisionService.send("\uD83C\uDD97 Marked %d as %s: %s (%d/%d)", mediaEntity.getId(), mediaEntity.getStatus(), mediaEntity, mediaEntity.getPartsCount(), mediaEntity.getAvailablePartsCount());
		}
	}
	
	private void updateFromOverseerr(@NonNull MediaEntity mediaEntity){
		if(Objects.isNull(mediaEntity.getOverseerrId())){
			log.warn("Cannot update media {} as it does not seem to be in Overseerr", mediaEntity);
			return;
		}
		try{
			var mediaDetails = overseerrApiService.getMediaDetails(mediaEntity.getOverseerrId(), mediaEntity.getType().getOverseerrType());
			
			Optional.ofNullable(switch(mediaDetails){
						case MovieMedia movieMedia -> movieMedia.getTitle();
						case SeriesMedia seriesMedia -> seriesMedia.getName();
						default -> null;
					})
					.ifPresent(mediaEntity::setName);
			Optional.ofNullable(mediaDetails.getMediaInfo())
					.map(MediaInfo::getRatingKey)
					.flatMap(key -> getActualRatingKey(mediaEntity, key))
					.ifPresent(mediaEntity::setPlexId);
			Optional.ofNullable(mediaDetails.getMediaInfo())
					.map(MediaInfo::getExternalServiceId)
					.ifPresent(mediaEntity::setServarrId);
			Optional.ofNullable(mediaDetails.getMediaInfo())
					.map(MediaInfo::getTvdbId)
					.ifPresent(mediaEntity::setTvdbId);
			Optional.ofNullable(mediaDetails.getMediaInfo())
					.map(MediaInfo::getTmdbId)
					.ifPresent(mediaEntity::setTmdbId);
			Optional.ofNullable(mediaDetails.getMediaInfo())
					.map(MediaInfo::getExternalServiceSlug)
					.ifPresent(slug -> {
						switch(mediaEntity.getType()){
							case MOVIE -> mediaEntity.setRadarrSlug(slug);
							case SEASON -> mediaEntity.setSonarrSlug(slug);
						}
					});
			
			// var partsCount = switch(mediaDetails){
			// 	case MovieMedia ignored -> 1;
			// 	case SeriesMedia seriesMedia -> seriesMedia.getSeasons().stream()
			// 			.filter(s -> Objects.equals(s.getSeasonNumber(), mediaEntity.getIndex()))
			// 			.findFirst()
			// 			.map(fr.rakambda.plexdeleter.api.overseerr.data.Season::getEpisodeCount)
			// 			.orElse(0);
			// 	default -> throw new UpdateException("Unexpected value: " + mediaDetails);
			// };
			// if(mediaEntity.getPartsCount() < partsCount){
			// 	mediaEntity.setPartsCount(partsCount);
			// }
		}
		catch(HttpServerErrorException.InternalServerError e){
			var body = e.getResponseBodyAsString();
			if(body.contains("Unable to retrieve movie.")){
				log.warn("Failed to update media from Overseerr");
				mediaEntity.setOverseerrId(null);
				supervisionService.send("❓ Media disappeared from Overseerr %s", mediaEntity);
				return;
			}
			log.error("Failed to update media from Overseerr, body was {}", body, e);
		}
		catch(Exception e){
			log.error("Failed to update media from Overseerr", e);
		}
	}
	
	private void updateFromTautulli(@NonNull MediaEntity mediaEntity){
		if(Objects.isNull(mediaEntity.getPlexId())){
			log.warn("Cannot update media {} as it does not seem to be in Plex/Tautulli", mediaEntity);
			return;
		}
		try{
			tautulliApiService.getMetadata(mediaEntity.getPlexId()).getResponse().getDataOptional()
					.flatMap(d -> Optional.ofNullable(d.getGrandparentGuid())
							.or(() -> Optional.ofNullable(d.getParentGuid()))
							.or(() -> Optional.ofNullable(d.getGuid())))
					.ifPresent(mediaEntity::setPlexGuid);
			
			var availablePartsCount = tautulliApiService.getElementsRatingKeys(mediaEntity.getPlexId(), mediaEntity.getType()).size();
			
			if(mediaEntity.getAvailablePartsCount() < availablePartsCount){
				mediaEntity.setAvailablePartsCount(availablePartsCount);
			}
		}
		catch(Exception e){
			log.error("Failed to update media from Tautulli", e);
		}
	}
	
	private void updateFromServarr(@NonNull MediaEntity mediaEntity, boolean forceMediaCount){
		if(Objects.isNull(mediaEntity.getServarrId())){
			log.warn("Cannot update media {} as it does not seem to be in Sonarr/Radarr", mediaEntity);
			return;
		}
		
		try{
			Optional<Integer> partsCount = Optional.empty();
			Optional<Integer> availablePartsCount = Optional.empty();
			
			switch(mediaEntity.getType()){
				case MOVIE -> {
					partsCount = Optional.of(1);
					var movie = radarrApiService.getMovie(mediaEntity.getServarrId());
					availablePartsCount = Optional.of(movie.isHasFile() ? 1 : 0);
					Optional.ofNullable(movie.getTmdbId()).ifPresent(mediaEntity::setTmdbId);
					Optional.ofNullable(movie.getTitleSlug()).ifPresent(mediaEntity::setRadarrSlug);
				}
				case SEASON -> {
					var series = sonarrApiService.getSeries(mediaEntity.getServarrId());
					var stats = series.getSeasons().stream()
							.filter(f -> Objects.equals(f.getSeasonNumber(), mediaEntity.getIndex()))
							.findFirst()
							.map(Season::getStatistics);
					Optional.ofNullable(series.getTvdbId()).ifPresent(mediaEntity::setTvdbId);
					Optional.ofNullable(series.getTitleSlug()).ifPresent(mediaEntity::setSonarrSlug);
					partsCount = stats.map(Statistics::getTotalEpisodeCount);
					availablePartsCount = stats.map(Statistics::getEpisodeFileCount);
				}
			}
			
			Predicate<Integer> partPredicate = forceMediaCount
					? v -> true
					: v -> mediaEntity.getPartsCount() < v;
			partsCount
					.filter(partPredicate)
					.ifPresent(mediaEntity::setPartsCount);
			availablePartsCount
					.filter(v -> mediaEntity.getAvailablePartsCount() < v)
					.ifPresent(mediaEntity::setAvailablePartsCount);
		}
		catch(HttpClientErrorException.NotFound e){
			log.warn("Failed to update media from Servarr, missing", e);
			mediaEntity.setServarrId(null);
			supervisionService.send("❓ Media disappeared from Servarr %s", mediaEntity);
		}
		catch(Exception e){
			log.error("Failed to update media from Servarr", e);
		}
	}
	
	@NonNull
	private Optional<Integer> getActualRatingKey(@NonNull MediaEntity mediaEntity, int ratingKey){
		try{
			return switch(mediaEntity.getType()){
				case SEASON -> tautulliApiService.getSeasonRatingKey(ratingKey, mediaEntity.getIndex());
				case MOVIE -> Optional.of(ratingKey);
			};
		}
		catch(RequestFailedException e){
			log.warn("Failed to get actual rating key", e);
			return Optional.empty();
		}
	}
	
	@NonNull
	public DeleteMediaResponse deleteMedia(@NonNull MediaEntity media, @Nullable UserGroupEntity userGroup, boolean deleteFromServarr) throws RequestFailedException{
		mediaOperationLock.lock();
		try{
			var deletedServarr = false;
			var deletedOverseerr = false;
			
			if(Objects.nonNull(userGroup)){
				deletedOverseerr = deleteMediaRequestsFromOverseerr(media, userGroup);
			}
			
			if(media.getRequirements().stream().map(MediaRequirementEntity::getStatus).anyMatch(MediaRequirementStatus::isWantToWatchMore)){
				return new DeleteMediaResponse(false, deletedServarr, deletedOverseerr);
			}
			
			if(deleteFromServarr){
				deletedServarr = deleteMediaFromServarr(media);
				
				media.setStatus(MediaStatus.PENDING_DELETION);
				mediaRepository.save(media);
			}
			
			supervisionService.send("\uD83D\uDCDB Media marked ready to delete %s", media);
			
			return new DeleteMediaResponse(false, deletedServarr, deletedOverseerr);
		}
		finally{
			mediaOperationLock.unlock();
		}
	}
	
	private boolean deleteMediaFromServarr(@NonNull MediaEntity media) throws RequestFailedException{
		if(Objects.isNull(media.getServarrId())){
			return false;
		}
		if(!media.getRequirements().stream().map(MediaRequirementEntity::getStatus).allMatch(MediaRequirementStatus::isCompleted)){
			return false;
		}
		
		if(media.getType() == MediaType.MOVIE){
			log.info("Unmonitor movie from Servarr {}", media);
			radarrApiService.unmonitor(media.getServarrId());
		}
		else if(media.getType() == MediaType.SEASON){
			log.info("Unmonitor season from Servarr {}", media);
			sonarrApiService.unmonitor(media.getServarrId(), media.getIndex());
			
			var seriesMedia = mediaRepository.findAllByServarrIdAndType(media.getServarrId(), media.getType());
			if(seriesMedia.size() <= 1
					|| seriesMedia.stream()
					.max(Comparator.comparingInt(MediaEntity::getIndex))
					.map(MediaEntity::getRequirements)
					.stream()
					.flatMap(Collection::stream)
					.map(MediaRequirementEntity::getStatus)
					.noneMatch(MediaRequirementStatus::isWantToWatchMore)
			){
				log.info("Unmonitor series from Servarr {}", media);
				sonarrApiService.unmonitor(media.getServarrId());
			}
			
			return true;
		}
		
		return false;
	}
	
	private boolean deleteMediaRequestsFromOverseerr(@NonNull MediaEntity media, @NonNull UserGroupEntity userGroup) throws RequestFailedException{
		if(Objects.isNull(media.getOverseerrId())){
			return false;
		}
		if(media.getAvailablePartsCount() > 0
				|| !media.getRequirements().stream().map(MediaRequirementEntity::getStatus).allMatch(MediaRequirementStatus::isCompleted)){
			return false;
		}
		
		var userIds = userGroup.getPersons().stream()
				.map(UserPersonEntity::getOverseerrId)
				.filter(Objects::nonNull)
				.toList();
		
		log.info("Deleting media request from Overseerr {} for group {}", media, userGroup);
		overseerrApiService.deleteRequestForUserAndMedia(userIds, media);
		return true;
	}
	
	@NonNull
	public MediaEntity addMedia(int overseerrId, @NonNull MediaType mediaType, int season, @Nullable Integer episode) throws RequestFailedException, UpdateException, NotifyException{
		mediaOperationLock.lock();
		try{
			log.info("Adding media with Overseerr id {}, season {}, episode {}", overseerrId, season, episode);
			var media = getOrCreateMedia(overseerrId, mediaType, season, episode);
			if(media.getStatus().isNeverChange() || media.getStatus().isOnDiskOrWillBe()){
				return media;
			}
			if(media.getStatus().isDeleted()){
				media.setLastRequestedTime(Instant.now());
			}
			media.setStatus(MediaStatus.WAITING);
			media.setAvailablePartsCount(0);
			return update(mediaRepository.save(media));
		}
		finally{
			mediaOperationLock.unlock();
		}
	}
	
	@NonNull
	public MediaEntity addMediaFromPrevious(@NonNull MediaEntity previousMedia, int season, @NonNull Instant addedDate) throws RequestFailedException, UpdateException, NotifyException{
		mediaOperationLock.lock();
		try{
			log.info("Adding media from previous {} season {}", previousMedia, season);
			var media = createMediaFromPrevious(previousMedia, season, addedDate);
			media.setStatus(MediaStatus.WAITING);
			media.setAvailablePartsCount(0);
			media = mediaRepository.save(media);
			
			return update(media);
		}
		finally{
			mediaOperationLock.unlock();
		}
	}
	
	@NonNull
	private MediaEntity getOrCreateMedia(int overseerrId, @NonNull MediaType mediaType, int season, @Nullable Integer episode) throws RequestFailedException{
		var media = episode == null
				? mediaRepository.findByOverseerrIdAndIndex(overseerrId, season)
				: mediaRepository.findByOverseerrIdAndIndexAndSubIndex(overseerrId, season, episode);
		if(media.isPresent()){
			return media.get();
		}
		return createMedia(overseerrId, mediaType, season, episode);
	}
	
	@NonNull
	private MediaEntity createMedia(int overseerrId, @NonNull MediaType mediaType, int season, @Nullable Integer episode) throws RequestFailedException{
		var mediaDetails = overseerrApiService.getMediaDetails(overseerrId, mediaType.getOverseerrType());
		var media = mediaRepository.save(MediaEntity.builder()
				.type(mediaType)
				.overseerrId(overseerrId)
				.name(switch(mediaDetails){
					case MovieMedia movieMedia -> movieMedia.getTitle();
					case SeriesMedia seriesMedia -> seriesMedia.getName();
					default -> throw new IllegalStateException("Unexpected value: " + mediaDetails);
				})
				.index(season)
				.subIndex(episode)
				.partsCount(0)
				.availablePartsCount(0)
				.status(MediaStatus.WAITING)
				.lastRequestedTime(Instant.now())
				.build());
		
		log.info("Creating new media {} for Overseerr id {}", media, overseerrId);
		supervisionService.send("\uD83C\uDD95 Added media %s", media);
		return media;
	}
	
	@NonNull
	private MediaEntity createMediaFromPrevious(@NonNull MediaEntity previous, int season, @NonNull Instant addedDate){
		var media = MediaEntity.builder()
				.type(previous.getType())
				.plexGuid(previous.getPlexGuid())
				.overseerrId(previous.getOverseerrId())
				.servarrId(previous.getServarrId())
				.tvdbId(previous.getTvdbId())
				.tmdbId(previous.getTmdbId())
				.sonarrSlug(previous.getSonarrSlug())
				.radarrSlug(previous.getRadarrSlug())
				.name(previous.getName())
				.index(season)
				.partsCount(0)
				.availablePartsCount(0)
				.status(MediaStatus.WAITING)
				.lastRequestedTime(addedDate)
				.build();
		
		log.info("Creating new media {} from previous {}", media, previous);
		supervisionService.send("\uD83C\uDD95 Added media %s", media);
		return media;
	}
	
	public void revertDeleteStatus(int mediaId) throws RequestFailedException, UpdateException, NotifyException{
		var mediaOptional = mediaRepository.findById(mediaId);
		if(mediaOptional.isEmpty()){
			return;
		}
		
		var media = mediaOptional.get();
		if(!media.getStatus().isCanBeDeleted()){
			return;
		}
		media.setStatus(MediaStatus.WAITING);
		media = mediaRepository.save(media);
		log.info("Reverted media {} status to {}", media, MediaStatus.WAITING);
		
		update(media);
	}
	
	public void keep(@NonNull MediaEntity media){
		media.setStatus(MediaStatus.KEEP);
		mediaRepository.save(media);
		
		log.info("Keeping media {}", media);
		supervisionService.send("\uD83D\uDCE6 Kept media %s", media);
	}
	
	public void unkeep(@NonNull MediaEntity media){
		media.setStatus(MediaStatus.WAITING);
		mediaRepository.save(media);
		
		log.info("Unkeeping media {}", media);
		supervisionService.send("\u26D3\uFE0F\u200D\uD83D\uDCA5 Unkept media %s", media);
	}
	
	public void manual(@NonNull MediaEntity media){
		media.setStatus(MediaStatus.MANUAL);
		mediaRepository.save(media);
		
		log.info("Setting manual media {}", media);
		supervisionService.send("🖐️ Manual media %s", media);
	}
	
	public void downloaded(@NonNull MediaEntity media) throws NotifyException{
		media.setStatus(MediaStatus.DOWNLOADED);
		mediaRepository.save(media);
		
		notificationService.notifyMediaAvailable(media);
		
		log.info("Setting downloaded media {}", media);
		supervisionService.send("🖐️ Downloaded media %s", media);
	}
	
	public void unmanual(@NonNull MediaEntity media){
		media.setStatus(MediaStatus.WAITING);
		mediaRepository.save(media);
		
		log.info("Setting automatic media {}", media);
		supervisionService.send("🤖 Automatic media %s", media);
	}
	
	public void manuallyDelete(@NonNull MediaEntity media) throws RequestFailedException{
		media.setStatus(MediaStatus.MANUALLY_DELETED);
		mediaRepository.save(media);
		if(Objects.nonNull(media.getServarrId())){
			switch(media.getType()){
				case MOVIE -> radarrApiService.unmonitor(media.getServarrId());
				case SEASON -> sonarrApiService.unmonitor(media.getServarrId(), media.getIndex());
			}
		}
		
		log.info("Manually deleted media {}", media);
		supervisionService.send("✍\uFE0F♻\uFE0F Manually deleted media %s", media);
	}
	
	public void updateMediaLabels(@NonNull MediaEntity media) throws RequestFailedException{
		if(Objects.isNull(media.getPlexId())){
			return;
		}
		log.info("Updating media labels for {}", media);
		
		var ratingKey = tautulliApiService.getMetadata(media.getPlexId()).getResponse().getDataOptional()
				.map(d -> Optional.ofNullable(d.getGrandparentRatingKey())
						.or(() -> Optional.ofNullable(d.getParentRatingKey()))
						.orElseGet(d::getRatingKey))
				.orElseGet(media::getPlexId);
		
		var collections = media.getRequirements().stream()
				.filter(mr -> mr.getStatus() == WAITING)
				.map(MediaRequirementEntity::getGroup)
				.filter(UserGroupEntity::getAppearInCollections)
				.map(UserGroupEntity::getName)
				.collect(Collectors.toSet());
		
		try{
			var currentCollections = plexMediaServerApiService.getElementMetadata(ratingKey).getMediaContainer().getMetadata().stream()
					.map(Metadata::getLabels)
					.filter(Objects::nonNull)
					.flatMap(Collection::stream)
					.map(Label::getTag)
					.collect(Collectors.toSet());
			
			if(currentCollections.contains("Overlay")){
				collections.add("Overlay");
			}
			
			if(currentCollections.equals(collections)){
				log.info("Labels are already correct");
				return;
			}
			
			plexMediaServerApiService.setElementLabels(ratingKey, collections);
		}
		catch(HttpClientErrorException.NotFound e){
			log.warn("Failed to label media, got 404, removing plex id from database");
			media.setPlexId(null);
			mediaRepository.save(media);
		}
	}
}
