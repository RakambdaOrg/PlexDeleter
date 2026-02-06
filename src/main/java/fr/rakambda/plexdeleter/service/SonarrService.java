package fr.rakambda.plexdeleter.service;

import fr.rakambda.plexdeleter.storage.entity.MediaType;
import fr.rakambda.plexdeleter.storage.repository.MediaRepository;
import fr.rakambda.plexdeleter.web.webhook.sonarr.data.Episode;
import fr.rakambda.plexdeleter.web.webhook.sonarr.data.SonarrWebhook;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SonarrService{
	private final MediaService mediaService;
	private final MediaRepository mediaRepository;
	
	public SonarrService(MediaService mediaService, MediaRepository mediaRepository){
		this.mediaService = mediaService;
		this.mediaRepository = mediaRepository;
	}
	
	public void onEpisodeGrabbed(@NonNull SonarrWebhook data){
		var series = data.getSeries();
		if(Objects.isNull(series) || Objects.isNull(series.getId())){
			log.warn("Not updating any media, could not determine sonarr id from {}", data);
			return;
		}
		
		var maxEpisodePerSeason = data.getEpisodes().stream()
				.collect(Collectors.toMap(Episode::getSeasonNumber, Episode::getEpisodeNumber, Math::max));
		
		for(var entry : maxEpisodePerSeason.entrySet()){
			var mediaEntity = mediaRepository.findByServarrIdAndIndexAndType(series.getId(), entry.getKey(), MediaType.SEASON);
			if(mediaEntity.isEmpty()){
				mediaService.updateAll();
				return;
			}
			
			mediaEntity.get().setPartsCount(Math.max(mediaEntity.get().getPartsCount(), entry.getValue()));
			mediaRepository.save(mediaEntity.get());
		}
	}
}
