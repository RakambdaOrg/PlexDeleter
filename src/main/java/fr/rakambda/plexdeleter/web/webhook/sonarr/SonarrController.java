package fr.rakambda.plexdeleter.web.webhook.sonarr;

import fr.rakambda.plexdeleter.storage.repository.MediaRepository;
import fr.rakambda.plexdeleter.web.webhook.sonarr.data.Episode;
import fr.rakambda.plexdeleter.web.webhook.sonarr.data.SonarrWebhook;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/webhook/sonarr")
public class SonarrController{
	private final MediaRepository mediaRepository;
	
	public SonarrController(MediaRepository mediaRepository){
		this.mediaRepository = mediaRepository;
	}
	
	@PostMapping
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void onCall(@NonNull SonarrWebhook data){
		switch(data.getEventType()){
			case "Grab" -> onEpisodeGrabbed(data);
		}
	}
	
	private void onEpisodeGrabbed(@NotNull SonarrWebhook data){
		var series = data.getSeries();
		if(Objects.isNull(series)){
			log.warn("Not updating any media, could not determine tvdb id from {}", data);
			return;
		}
		
		var maxEpisodePerSeason = series.getEpisodes().stream()
				.collect(Collectors.toMap(Episode::getSeasonNumber, Episode::getEpisodeNumber, Math::max));
		
		for(var entry : maxEpisodePerSeason.entrySet()){
			var mediaEntity = mediaRepository.findByTvdbIdAndIndex(series.getTvdbId(), entry.getKey());
			if(mediaEntity.isEmpty()){
				log.warn("Not updating any media, could not find media with tvdb id {}, season {}, from {}", series.getTvdbId(), entry.getKey(), data);
				return;
			}
			
			mediaEntity.get().setPartsCount(Math.max(mediaEntity.get().getPartsCount(), entry.getValue()));
			mediaRepository.save(mediaEntity.get());
		}
	}
}
