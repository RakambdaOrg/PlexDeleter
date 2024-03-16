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
import fr.rakambda.plexdeleter.storage.entity.MediaActionStatus;
import fr.rakambda.plexdeleter.storage.entity.MediaAvailability;
import fr.rakambda.plexdeleter.storage.entity.MediaEntity;
import fr.rakambda.plexdeleter.storage.entity.MediaRequirementEntity;
import fr.rakambda.plexdeleter.storage.entity.MediaRequirementStatus;
import fr.rakambda.plexdeleter.storage.entity.MediaType;
import fr.rakambda.plexdeleter.storage.entity.UserGroupEntity;
import fr.rakambda.plexdeleter.storage.repository.MediaRepository;
import fr.rakambda.plexdeleter.storage.repository.UserGroupRepository;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
	private final UserGroupRepository userGroupRepository;
	private final Lock mediaOperationLock;
	
	@Autowired
	public MediaService(TautulliService tautulliService, SupervisionService supervisionService, MediaRepository mediaRepository, OverseerrService overseerrService, SonarrService sonarrService, RadarrService radarrService, NotificationService notificationService, UserGroupRepository userGroupRepository){
		this.tautulliService = tautulliService;
		this.supervisionService = supervisionService;
		this.mediaRepository = mediaRepository;
		this.overseerrService = overseerrService;
		this.sonarrService = sonarrService;
		this.radarrService = radarrService;
		this.notificationService = notificationService;
		this.userGroupRepository = userGroupRepository;
		this.mediaOperationLock = new ReentrantLock();
	}
	
	@NotNull
	public MediaEntity update(int mediaId) throws UpdateException, RequestFailedException, NotifyException{
		mediaOperationLock.lock();
		try{
			var mediaEntity = mediaRepository.findById(mediaId)
					.orElseThrow(() -> new UpdateException("Media with id %d not found".formatted(mediaId)));
			log.info("Updating mediaOptional {}", mediaEntity);
			
			updateFromOverseerr(mediaEntity);
			updateFromTautulli(mediaEntity);
			updateFromServarr(mediaEntity);
			
			if(mediaEntity.getPartsCount() <= 0){
				log.warn("Failed to update {}, not enough info", mediaEntity);
				supervisionService.send("\uD83D\uDEAB Could not update %s", mediaEntity);
			}
			else if(mediaEntity.getAvailablePartsCount() >= mediaEntity.getPartsCount()){
				mediaEntity.setAvailability(MediaAvailability.DOWNLOADED);
				log.info("Marked mediaOptional {} as finished", mediaEntity);
				notificationService.notifyMediaAvailable(mediaEntity);
				supervisionService.send("\uD83C\uDD97 Marked %d as downloaded: %s (%d/%d)", mediaEntity.getId(), mediaEntity, mediaEntity.getPartsCount(), mediaEntity.getAvailablePartsCount());
			}
			else if(mediaEntity.getAvailablePartsCount() > 0){
				mediaEntity.setAvailability(MediaAvailability.DOWNLOADING);
			}
			
			return mediaRepository.save(mediaEntity);
		}
		finally{
			mediaOperationLock.unlock();
		}
	}
	
	private void updateFromOverseerr(@NotNull MediaEntity mediaEntity) throws UpdateException, RequestFailedException{
		if(Objects.isNull(mediaEntity.getOverseerrId())){
			log.warn("Cannot update mediaOptional {} as it does not seem to be in Overseerr", mediaEntity);
			return;
		}
		var mediaDetails = overseerrService.getMediaDetails(mediaEntity.getOverseerrId(), mediaEntity.getType().getOverseerrType());
		
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
		
		var partsCount = switch(mediaDetails){
			case MovieMedia ignored -> 1;
			case SeriesMedia seriesMedia -> seriesMedia.getSeasons().stream()
					.filter(s -> Objects.equals(s.getSeasonNumber(), mediaEntity.getIndex()))
					.findFirst()
					.map(fr.rakambda.plexdeleter.api.overseerr.data.Season::getEpisodeCount)
					.orElse(0);
			default -> throw new UpdateException("Unexpected value: " + mediaDetails);
		};
		if(mediaEntity.getPartsCount() < partsCount){
			mediaEntity.setPartsCount(partsCount);
		}
	}
	
	@NotNull
	private Optional<Integer> getActualRatingKey(@NotNull MediaEntity mediaEntity, int ratingKey){
		try{
			return switch(mediaEntity.getType()){
				case SEASON -> tautulliService.getSeasonRatingKey(ratingKey, mediaEntity.getIndex());
				case MOVIE -> Optional.of(ratingKey);
			};
		}
		catch(RequestFailedException e){
			log.warn("Failed to get actual rating key", e);
			return Optional.empty();
		}
	}
	
	private void updateFromTautulli(@NotNull MediaEntity mediaEntity) throws RequestFailedException{
		if(Objects.isNull(mediaEntity.getPlexId())){
			log.warn("Cannot update mediaOptional {} as it does not seem to be in Plex/Tautulli", mediaEntity);
			return;
		}
		var availablePartsCount = tautulliService.getElementsRatingKeys(mediaEntity.getPlexId(), mediaEntity.getType()).size();
		
		if(mediaEntity.getAvailablePartsCount() < availablePartsCount){
			mediaEntity.setAvailablePartsCount(availablePartsCount);
		}
	}
	
	private void updateFromServarr(@NotNull MediaEntity mediaEntity) throws RequestFailedException{
		if(Objects.isNull(mediaEntity.getServarrId())){
			log.warn("Cannot update mediaOptional {} as it does not seem to be in Sonarr/Radarr", mediaEntity);
			return;
		}
		
		Optional<Integer> partsCount = Optional.empty();
		Optional<Integer> availablePartsCount = Optional.empty();
		
		switch(mediaEntity.getType()){
			case MOVIE -> {
				partsCount = Optional.of(1);
				availablePartsCount = Optional.of(radarrService.getMovie(mediaEntity.getServarrId()).isHasFile() ? 1 : 0);
			}
			case SEASON -> {
				var stats = sonarrService.getSeries(mediaEntity.getServarrId()).getSeasons().stream()
						.filter(f -> Objects.equals(f.getSeasonNumber(), mediaEntity.getIndex()))
						.findFirst()
						.map(Season::getStatistics);
				partsCount = stats.map(Statistics::getTotalEpisodeCount);
				availablePartsCount = stats.map(Statistics::getEpisodeFileCount);
			}
		}
		
		partsCount
				.filter(v -> mediaEntity.getPartsCount() < v)
				.ifPresent(mediaEntity::setPartsCount);
		availablePartsCount
				.filter(v -> mediaEntity.getAvailablePartsCount() < v)
				.ifPresent(mediaEntity::setAvailablePartsCount);
	}
	
	public void deleteMedia(int mediaId, boolean deleteFromServices) throws NotifyException, RequestFailedException{
		mediaOperationLock.lock();
		try{
			var mediaOptional = mediaRepository.findById(mediaId);
			if(mediaOptional.isEmpty()){
				return;
			}
			var media = mediaOptional.get();
			
			log.info("Deleting media {}", media);
			var groups = media.getRequirements().stream()
					.map(MediaRequirementEntity::getGroup)
					.toList();
			
			if(deleteFromServices
					&& media.getAvailablePartsCount() <= 0
					&& media.getRequirements().stream().map(MediaRequirementEntity::getStatus).allMatch(MediaRequirementStatus::isCompleted)){
				if(Objects.nonNull(media.getServarrId())){
					switch(media.getType()){
						case MOVIE -> radarrService.delete(media.getServarrId());
						case SEASON -> sonarrService.delete(media.getServarrId());
					}
				}
				
				if(Objects.nonNull(media.getOverseerrId())){
					overseerrService.deleteRequestForMedia(media.getOverseerrId());
				}
			}
			
			mediaRepository.delete(media);
			
			for(var group : groups){
				notificationService.notifyMediaDeleted(group, media);
			}
			supervisionService.send("\uD83D\uDCDB Media deleted from database %s", media);
		}
		finally{
			mediaOperationLock.unlock();
		}
	}
	
	public int addMedia(@NotNull UserGroupEntity userGroupEntity, int overseerrId, @NotNull MediaType mediaType, int season) throws RequestFailedException, UpdateException, NotifyException{
		mediaOperationLock.lock();
		try{
			log.info("Adding mediaOptional with Overseerr id {} and season {} to {}", overseerrId, season, userGroupEntity);
			var media = getOrCreateMedia(overseerrId, mediaType, season);
			media.setAvailability(MediaAvailability.WAITING);
			media.setActionStatus(MediaActionStatus.TO_DELETE);
			media.setAvailablePartsCount(0);
			mediaRepository.save(media);
			
			return update(media.getId()).getId();
		}
		finally{
			mediaOperationLock.unlock();
		}
	}
	
	@NotNull
	private MediaEntity getOrCreateMedia(int overseerrId, @NotNull MediaType mediaType, int season) throws RequestFailedException{
		var media = mediaRepository.findByOverseerrIdAndIndex(overseerrId, season);
		if(media.isPresent()){
			return media.get();
		}
		return createMedia(overseerrId, mediaType, season);
	}
	
	@NotNull
	private MediaEntity createMedia(int overseerrId, @NotNull MediaType mediaType, int season) throws RequestFailedException{
		var mediaDetails = overseerrService.getMediaDetails(overseerrId, mediaType.getOverseerrType());
		var media = MediaEntity.builder()
				.type(mediaType)
				.overseerrId(overseerrId)
				.name(switch(mediaDetails){
					case MovieMedia movieMedia -> movieMedia.getTitle();
					case SeriesMedia seriesMedia -> seriesMedia.getName();
					default -> throw new IllegalStateException("Unexpected value: " + mediaDetails);
				})
				.index(season)
				.partsCount(0)
				.availablePartsCount(0)
				.availability(MediaAvailability.WAITING)
				.actionStatus(MediaActionStatus.TO_DELETE)
				.build();
		
		log.info("Creating new mediaOptional {} for Overseerr id {}", media, overseerrId);
		supervisionService.send("\uD83C\uDD95 Added mediaOptional %s", media);
		return media;
	}
}
