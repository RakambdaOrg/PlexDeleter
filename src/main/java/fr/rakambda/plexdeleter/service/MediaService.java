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
import fr.rakambda.plexdeleter.storage.entity.MediaAvailability;
import fr.rakambda.plexdeleter.storage.entity.MediaEntity;
import fr.rakambda.plexdeleter.storage.repository.MediaRepository;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Objects;
import java.util.Optional;

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
	
	@Autowired
	public MediaService(TautulliService tautulliService, SupervisionService supervisionService, MediaRepository mediaRepository, OverseerrService overseerrService, SonarrService sonarrService, RadarrService radarrService, NotificationService notificationService){
		this.tautulliService = tautulliService;
		this.supervisionService = supervisionService;
		this.mediaRepository = mediaRepository;
		this.overseerrService = overseerrService;
		this.sonarrService = sonarrService;
		this.radarrService = radarrService;
		this.notificationService = notificationService;
	}
	
	@NotNull
	public MediaEntity update(@NotNull MediaEntity mediaEntity) throws UpdateException, RequestFailedException, NotifyException{
		log.info("Updating media {}", mediaEntity);
		updateFromOverseerr(mediaEntity);
		updateFromTautulli(mediaEntity);
		updateFromServarr(mediaEntity);
		
		if(mediaEntity.getPartsCount() <= 0){
			log.warn("Failed to update {}, not enough info", mediaEntity);
			supervisionService.send("❌ Could not update %s", mediaEntity);
		}
		else if(mediaEntity.getAvailablePartsCount() >= mediaEntity.getPartsCount()){
			mediaEntity.setAvailability(MediaAvailability.DOWNLOADED);
			log.info("Marked media {} as finished", mediaEntity);
			notificationService.notifyMediaAvailable(mediaEntity);
			supervisionService.send("\uD83C\uDD97 Marked %d as finished: %s (%d/%d)", mediaEntity.getId(), mediaEntity, mediaEntity.getPartsCount(), mediaEntity.getAvailablePartsCount());
		}
		
		return mediaRepository.save(mediaEntity);
	}
	
	private void updateFromOverseerr(@NotNull MediaEntity mediaEntity) throws UpdateException, RequestFailedException{
		if(Objects.isNull(mediaEntity.getOverseerrId())){
			log.warn("Cannot update media {} as it does not seem to be in Overseerr", mediaEntity);
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
			log.warn("Cannot update media {} as it does not seem to be in Plex/Tautulli", mediaEntity);
			return;
		}
		var availablePartsCount = tautulliService.getElementsRatingKeys(mediaEntity.getPlexId(), mediaEntity.getType()).size();
		
		if(mediaEntity.getAvailablePartsCount() < availablePartsCount){
			mediaEntity.setAvailablePartsCount(availablePartsCount);
		}
	}
	
	private void updateFromServarr(@NotNull MediaEntity mediaEntity) throws RequestFailedException{
		if(Objects.isNull(mediaEntity.getServarrId())){
			log.warn("Cannot update media {} as it does not seem to be in Sonarr/Radarr", mediaEntity);
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
}
