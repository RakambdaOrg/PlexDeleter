package fr.rakambda.plexdeleter.service;

import fr.rakambda.plexdeleter.api.RequestFailedException;
import fr.rakambda.plexdeleter.api.overseerr.OverseerrService;
import fr.rakambda.plexdeleter.api.overseerr.data.MediaInfo;
import fr.rakambda.plexdeleter.api.overseerr.data.MovieMedia;
import fr.rakambda.plexdeleter.api.overseerr.data.SeriesMedia;
import fr.rakambda.plexdeleter.api.servarr.radarr.RadarrService;
import fr.rakambda.plexdeleter.api.servarr.sonarr.SonarrService;
import fr.rakambda.plexdeleter.api.servarr.sonarr.data.Season;
import fr.rakambda.plexdeleter.api.servarr.sonarr.data.Statistics;
import fr.rakambda.plexdeleter.api.tautulli.TautulliService;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;

@Slf4j
@Service
public class MediaService{
	private final TautulliService tautulliService;
	private final SupervisionService supervisionService;
	private final MediaRepository mediaRepository;
	private final OverseerrService overseerrService;
	private final SonarrService sonarrService;
	private final RadarrService radarrService;
	private final NotificationService notificationService;
	private final Lock mediaOperationLock;
	
	@Autowired
	public MediaService(TautulliService tautulliService, SupervisionService supervisionService, MediaRepository mediaRepository, OverseerrService overseerrService, SonarrService sonarrService, RadarrService radarrService, NotificationService notificationService){
		this.tautulliService = tautulliService;
		this.supervisionService = supervisionService;
		this.mediaRepository = mediaRepository;
		this.overseerrService = overseerrService;
		this.sonarrService = sonarrService;
		this.radarrService = radarrService;
		this.notificationService = notificationService;
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
	
	@NotNull
	public MediaEntity update(@NotNull MediaEntity mediaEntity) throws UpdateException, RequestFailedException, NotifyException{
		return update(mediaEntity, false);
	}
	
	@NotNull
	public MediaEntity update(@NotNull MediaEntity mediaEntity, boolean forceMediaCount) throws UpdateException, RequestFailedException, NotifyException{
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
	
	private void handleRequirements(@NotNull MediaEntity mediaEntity){
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
	
	private void updateFromOverseerr(@NotNull MediaEntity mediaEntity){
		if(Objects.isNull(mediaEntity.getOverseerrId())){
			log.warn("Cannot update media {} as it does not seem to be in Overseerr", mediaEntity);
			return;
		}
		try{
			var mediaDetails = overseerrService.getMediaDetails(mediaEntity.getOverseerrId(), mediaEntity.getType().getOverseerrType());
			
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
					.map(MediaInfo::getRatingKey)
					.ifPresent(mediaEntity::setRootPlexId);
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
							case SEASON, EPISODE -> mediaEntity.setSonarrSlug(slug);
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
		catch(Exception e){
			log.error("Failed to update media from Overseerr", e);
		}
	}
	
	private void updateFromTautulli(@NotNull MediaEntity mediaEntity){
		if(Objects.isNull(mediaEntity.getPlexId())){
			log.warn("Cannot update media {} as it does not seem to be in Plex/Tautulli", mediaEntity);
			return;
		}
		try{
			var availablePartsCount = tautulliService.getElementsRatingKeys(mediaEntity.getPlexId(), mediaEntity.getType()).size();
			
			if(mediaEntity.getAvailablePartsCount() < availablePartsCount){
				mediaEntity.setAvailablePartsCount(availablePartsCount);
			}
		}
		catch(Exception e){
			log.error("Failed to update media from Tautulli", e);
		}
	}
	
	private void updateFromServarr(@NotNull MediaEntity mediaEntity, boolean forceMediaCount){
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
					var movie = radarrService.getMovie(mediaEntity.getServarrId());
					availablePartsCount = Optional.of(movie.isHasFile() ? 1 : 0);
					Optional.ofNullable(movie.getTmdbId()).ifPresent(mediaEntity::setTmdbId);
					Optional.ofNullable(movie.getTitleSlug()).ifPresent(mediaEntity::setRadarrSlug);
				}
				case SEASON -> {
					var series = sonarrService.getSeries(mediaEntity.getServarrId());
					var stats = series.getSeasons().stream()
							.filter(f -> Objects.equals(f.getSeasonNumber(), mediaEntity.getIndex()))
							.findFirst()
							.map(Season::getStatistics);
					Optional.ofNullable(series.getTvdbId()).ifPresent(mediaEntity::setTvdbId);
					Optional.ofNullable(series.getTitleSlug()).ifPresent(mediaEntity::setSonarrSlug);
					partsCount = stats.map(Statistics::getTotalEpisodeCount);
					availablePartsCount = stats.map(Statistics::getEpisodeFileCount);
				}
				case EPISODE -> {
					partsCount = Optional.of(1);
					var series = sonarrService.getSeries(mediaEntity.getServarrId());
					var stats = series.getSeasons().stream()
							.filter(f -> Objects.equals(f.getSeasonNumber(), mediaEntity.getIndex()))
							.findFirst()
							.map(Season::getStatistics);
					Optional.ofNullable(series.getTvdbId()).ifPresent(mediaEntity::setTvdbId);
					Optional.ofNullable(series.getTitleSlug()).ifPresent(mediaEntity::setSonarrSlug);
					availablePartsCount = stats.map(Statistics::getEpisodeFileCount).map(v -> Math.min(1, v));
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
		catch(Exception e){
			log.error("Failed to update media from Servarr", e);
		}
	}
	
	@NotNull
	private Optional<Integer> getActualRatingKey(@NotNull MediaEntity mediaEntity, int ratingKey){
		try{
			return switch(mediaEntity.getType()){
				case SEASON -> tautulliService.getSeasonRatingKey(ratingKey, mediaEntity.getIndex());
				case EPISODE, MOVIE -> Optional.of(ratingKey);
			};
		}
		catch(RequestFailedException e){
			log.warn("Failed to get actual rating key", e);
			return Optional.empty();
		}
	}
	
	@NotNull
	public DeleteMediaResponse deleteMedia(@NotNull MediaEntity media, @Nullable UserGroupEntity userGroup, boolean deleteFromServarr) throws NotifyException, RequestFailedException{
		mediaOperationLock.lock();
		try{
			var groups = media.getRequirements().stream()
					.map(MediaRequirementEntity::getGroup)
					.toList();
			
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
			}
			
			media.setStatus(MediaStatus.PENDING_DELETION);
			mediaRepository.delete(media);
			//
			// for(var group : groups){
			// 	notificationService.notifyMediaDeleted(group, media);
			// }
			supervisionService.send("\uD83D\uDCDB Media marked ready to delete %s", media);
			
			return new DeleteMediaResponse(false, deletedServarr, deletedOverseerr);
		}
		finally{
			mediaOperationLock.unlock();
		}
	}
	
	private boolean deleteMediaFromServarr(@NotNull MediaEntity media) throws RequestFailedException{
		if(Objects.isNull(media.getServarrId())){
			return false;
		}
		if(media.getAvailablePartsCount() > 0
				|| !media.getRequirements().stream().map(MediaRequirementEntity::getStatus).allMatch(MediaRequirementStatus::isCompleted)){
			return false;
		}
		
		if(media.getType() == MediaType.SEASON && mediaRepository.countByServarrIdAndType(media.getServarrId(), media.getType()) > 1){
			log.info("Unmonitor media from Servarr {}", media);
			sonarrService.unmonitor(media.getServarrId(), media.getIndex());
			return true;
		}
		
		log.info("Deleting media from Servarr {}", media);
		switch(media.getType()){
			case MOVIE -> radarrService.delete(media.getServarrId());
			case SEASON -> sonarrService.delete(media.getServarrId());
		}
		return true;
	}
	
	private boolean deleteMediaRequestsFromOverseerr(@NotNull MediaEntity media, @NotNull UserGroupEntity userGroup) throws RequestFailedException{
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
		overseerrService.deleteRequestForUserAndMedia(userIds, media);
		return true;
	}
	
	@NotNull
	public MediaEntity addMedia(int overseerrId, @NotNull MediaType mediaType, int season, @Nullable Integer episode) throws RequestFailedException, UpdateException, NotifyException{
		mediaOperationLock.lock();
		try{
			log.info("Adding media with Overseerr id {}, season {}, episode {}", overseerrId, season, episode);
			var media = getOrCreateMedia(overseerrId, mediaType, season, episode);
			if(media.getStatus().isNeverChange() || media.getStatus().isOnDiskOrWillBe()){
				return media;
			}
			media.setStatus(MediaStatus.WAITING);
			media.setAvailablePartsCount(0);
			return update(mediaRepository.save(media));
		}
		finally{
			mediaOperationLock.unlock();
		}
	}
	
	@NotNull
	public MediaEntity addMediaFromPrevious(@NotNull MediaEntity previousMedia, int season) throws RequestFailedException, UpdateException, NotifyException{
		mediaOperationLock.lock();
		try{
			log.info("Adding media from previous {} season {}", previousMedia, season);
			var media = createMediaFromPrevious(previousMedia, season);
			media.setStatus(MediaStatus.WAITING);
			media.setAvailablePartsCount(0);
			media = mediaRepository.save(media);
			
			return update(media);
		}
		finally{
			mediaOperationLock.unlock();
		}
	}
	
	@NotNull
	private MediaEntity getOrCreateMedia(int overseerrId, @NotNull MediaType mediaType, int season, @Nullable Integer episode) throws RequestFailedException{
		var media = episode == null
				? mediaRepository.findByOverseerrIdAndIndex(overseerrId, season)
				: mediaRepository.findByOverseerrIdAndIndexAndSubIndex(overseerrId, season, episode);
		if(media.isPresent()){
			return media.get();
		}
		return createMedia(overseerrId, mediaType, season, episode);
	}
	
	@NotNull
	private MediaEntity createMedia(int overseerrId, @NotNull MediaType mediaType, int season, @Nullable Integer episode) throws RequestFailedException{
		var mediaDetails = overseerrService.getMediaDetails(overseerrId, mediaType.getOverseerrType());
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
				.build());
		
		log.info("Creating new media {} for Overseerr id {}", media, overseerrId);
		supervisionService.send("\uD83C\uDD95 Added media %s", media);
		return media;
	}
	
	@NotNull
	private MediaEntity createMediaFromPrevious(@NotNull MediaEntity previous, int season){
		var media = MediaEntity.builder()
				.type(previous.getType())
				.rootPlexId(previous.getRootPlexId())
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
	
	public void keep(@NotNull MediaEntity media){
		media.setStatus(MediaStatus.KEEP);
		mediaRepository.save(media);
		
		log.info("Keeping media {}", media);
		supervisionService.send("\uD83D\uDCE6 Kept media %s", media);
	}
	
	public void unkeep(@NotNull MediaEntity media){
		media.setStatus(MediaStatus.WAITING);
		mediaRepository.save(media);
		
		log.info("Unkeeping media {}", media);
		supervisionService.send("\u26D3\uFE0F\u200D\uD83D\uDCA5 Unkept media %s", media);
	}
	
	public void manuallyDelete(@NotNull MediaEntity media){
		media.setStatus(MediaStatus.MANUALLY_DELETED);
		mediaRepository.save(media);
		
		log.info("Manually deleted media {}", media);
		supervisionService.send("✍\uFE0F♻\uFE0F Manually deleted media %s", media);
	}
}
