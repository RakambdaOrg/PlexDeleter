package fr.rakambda.plexdeleter.schedule;

import fr.rakambda.plexdeleter.SupervisionService;
import fr.rakambda.plexdeleter.api.RequestFailedException;
import fr.rakambda.plexdeleter.api.overseerr.OverseerrService;
import fr.rakambda.plexdeleter.api.overseerr.data.ExternalIds;
import fr.rakambda.plexdeleter.api.overseerr.data.MediaInfo;
import fr.rakambda.plexdeleter.api.overseerr.data.MovieMedia;
import fr.rakambda.plexdeleter.api.overseerr.data.SeriesMedia;
import fr.rakambda.plexdeleter.api.radarr.RadarrService;
import fr.rakambda.plexdeleter.api.sonarr.SonarrService;
import fr.rakambda.plexdeleter.api.sonarr.data.Season;
import fr.rakambda.plexdeleter.api.sonarr.data.Statistics;
import fr.rakambda.plexdeleter.api.tautulli.TautulliService;
import fr.rakambda.plexdeleter.storage.entity.MediaAvailability;
import fr.rakambda.plexdeleter.storage.entity.MediaEntity;
import fr.rakambda.plexdeleter.storage.repository.MediaRepository;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Component
public class UpdateMediaScheduler implements IScheduler{
	private final MediaRepository mediaRepository;
	private final OverseerrService overseerrService;
	private final TautulliService tautulliService;
	private final SonarrService sonarrService;
	private final RadarrService radarrService;
	private final SupervisionService supervisionService;
	
	@Autowired
	public UpdateMediaScheduler(MediaRepository mediaRepository, OverseerrService overseerrService, TautulliService tautulliService, SonarrService sonarrService, RadarrService radarrService, SupervisionService supervisionService){
		this.mediaRepository = mediaRepository;
		this.overseerrService = overseerrService;
		this.tautulliService = tautulliService;
		this.sonarrService = sonarrService;
		this.radarrService = radarrService;
		this.supervisionService = supervisionService;
	}
	
	@Override
	@Scheduled(cron = "0 0 */6 * * *")
	public void run(){
		var medias = mediaRepository.findAllByAvailabilityIs(MediaAvailability.DOWNLOADING);
		for(var media : medias){
			try{
				update(media);
			}
			catch(UpdateException | RequestFailedException | IOException e){
				log.error("Failed to update media {}", media, e);
			}
		}
		
		log.info("Done updating {} media", medias.size());
	}
	
	@VisibleForTesting
	void update(MediaEntity mediaEntity) throws UpdateException, RequestFailedException, IOException{
		log.info("Updating media {}", mediaEntity);
		updateFromOverseerr(mediaEntity);
		updateFromTautulli(mediaEntity);
		updateFromServarr(mediaEntity);
		
		if(Objects.isNull(mediaEntity.getPartsCount()) || Objects.isNull(mediaEntity.getAvailablePartsCount())){
			log.warn("Failed to update {}, not enough info", mediaEntity);
			supervisionService.send("❌ Could not update %s", mediaEntity);
		}
		else if(mediaEntity.getPartsCount() >= mediaEntity.getAvailablePartsCount()){
			mediaEntity.setAvailability(MediaAvailability.DOWNLOADED);
			log.info("Marked media {} as finished", mediaEntity);
			supervisionService.send("\uD83C\uDD97 Marked %d as finished: %s (%d/%d)", mediaEntity.getId(), mediaEntity, mediaEntity.getPartsCount(), mediaEntity.getAvailablePartsCount());
		}
		
		mediaRepository.save(mediaEntity);
	}
	
	private void updateFromOverseerr(@NotNull MediaEntity mediaEntity) throws UpdateException, RequestFailedException{
		if(Objects.isNull(mediaEntity.getOverseerrId())){
			log.warn("Cannot update media {} as it does not seem to be in Overseerr", mediaEntity);
			return;
		}
		var mediaDetails = overseerrService.getMediaDetails(mediaEntity.getOverseerrId(), mediaEntity.getType());
		
		Optional.ofNullable(mediaDetails.getMediaInfo())
				.map(MediaInfo::getRatingKey)
				.ifPresent(mediaEntity::setPlexId);
		Optional.ofNullable(mediaDetails.getMediaInfo())
				.map(MediaInfo::getExternalServiceId)
				.ifPresent(mediaEntity::setServarrId);
		Optional.ofNullable(mediaDetails.getExternalIds())
				.map(ExternalIds::getTvdbId)
				.ifPresent(mediaEntity::setTvdbId);
		
		var partsCount = switch(mediaDetails){
			case MovieMedia ignored -> 1;
			case SeriesMedia seriesMedia -> seriesMedia.getNumberOfEpisodes();
			default -> throw new UpdateException("Unexpected value: " + mediaDetails);
		};
		if(Objects.isNull(mediaEntity.getPartsCount()) || mediaEntity.getPartsCount() < partsCount){
			mediaEntity.setPartsCount(partsCount);
		}
	}
	
	private void updateFromTautulli(@NotNull MediaEntity mediaEntity) throws RequestFailedException{
		if(Objects.isNull(mediaEntity.getPlexId())){
			log.warn("Cannot update media {} as it does not seem to be in Plex/Tautulli", mediaEntity);
			return;
		}
		var availablePartsCount = tautulliService.getElementsRatingKeys(mediaEntity.getPlexId(), mediaEntity.getType()).size();
		
		if(Objects.isNull(mediaEntity.getAvailablePartsCount()) || mediaEntity.getAvailablePartsCount() < availablePartsCount){
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
				.filter(v -> Objects.isNull(mediaEntity.getPartsCount()) || mediaEntity.getPartsCount() < v)
				.ifPresent(mediaEntity::setPartsCount);
		availablePartsCount
				.filter(v -> Objects.isNull(mediaEntity.getAvailablePartsCount()) || mediaEntity.getAvailablePartsCount() < v)
				.ifPresent(mediaEntity::setAvailablePartsCount);
	}
	
	@VisibleForTesting
	static class UpdateException extends Exception{
		public UpdateException(@NotNull String message){
			super(message);
		}
	}
}
