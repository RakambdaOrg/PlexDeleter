package fr.rakambda.plexdeleter.service;

import fr.rakambda.plexdeleter.storage.repository.MediaRepository;
import fr.rakambda.plexdeleter.web.webhook.radarr.data.RadarrWebhook;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import java.util.Objects;

@Slf4j
@Service
public class RadarrService{
	private final MediaService mediaService;
	private final MediaRepository mediaRepository;
	
	public RadarrService(MediaService mediaService, MediaRepository mediaRepository){
		this.mediaService = mediaService;
		this.mediaRepository = mediaRepository;
	}
	
	public void onMovieGrabbed(@NonNull RadarrWebhook data){
		var movie = data.getMovie();
		if(Objects.isNull(movie) || Objects.isNull(movie.getId())){
			log.warn("Not updating any media, could not determine radarr id from {}", data);
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
