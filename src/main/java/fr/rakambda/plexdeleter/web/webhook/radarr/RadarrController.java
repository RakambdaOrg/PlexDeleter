package fr.rakambda.plexdeleter.web.webhook.radarr;

import fr.rakambda.plexdeleter.service.MediaService;
import fr.rakambda.plexdeleter.storage.repository.MediaRepository;
import fr.rakambda.plexdeleter.web.webhook.radarr.data.RadarrWebhook;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import java.util.Objects;

@Slf4j
@RestController
@RequestMapping("/webhook/radarr")
public class RadarrController{
	private final MediaRepository mediaRepository;
	private final MediaService mediaService;
	
	@Autowired
	public RadarrController(MediaRepository mediaRepository, MediaService mediaService){
		this.mediaRepository = mediaRepository;
		this.mediaService = mediaService;
	}
	
	@PostMapping
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void onCall(@NotNull @RequestBody RadarrWebhook data){
		log.info("Received new Radarr webhook {}", data);
		switch(data.getEventType()){
			case "Grab" -> onEpisodeGrabbed(data);
		}
	}
	
	private void onEpisodeGrabbed(@NonNull RadarrWebhook data){
		var movie = data.getMovie();
		if(Objects.isNull(movie)){
			log.warn("Not updating any media, could not determine tmdb id from {}", data);
			return;
		}
		
		var mediaEntity = mediaRepository.findByServarrIdAndIndex(movie.getId(), 1);
		if(mediaEntity.isEmpty()){
			mediaService.updateAll();
			return;
		}
		
		mediaEntity.get().setPartsCount(Math.max(mediaEntity.get().getPartsCount(), 1));
		mediaRepository.save(mediaEntity.get());
	}
}
