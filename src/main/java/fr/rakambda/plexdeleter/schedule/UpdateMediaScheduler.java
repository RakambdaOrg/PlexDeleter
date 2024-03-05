package fr.rakambda.plexdeleter.schedule;

import fr.rakambda.plexdeleter.api.RequestFailedException;
import fr.rakambda.plexdeleter.service.MediaService;
import fr.rakambda.plexdeleter.service.UpdateException;
import fr.rakambda.plexdeleter.storage.entity.MediaAvailability;
import fr.rakambda.plexdeleter.storage.repository.MediaRepository;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UpdateMediaScheduler implements IScheduler{
	private final MediaRepository mediaRepository;
	private final MediaService mediaService;
	
	@Autowired
	public UpdateMediaScheduler(MediaRepository mediaRepository, MediaService mediaService){
		this.mediaRepository = mediaRepository;
		this.mediaService = mediaService;
	}
	
	@Override
	@NotNull
	public String getTaskId(){
		return "media-update";
	}
	
	@Override
	@Scheduled(cron = "0 0 0,8,15 * * *")
	public void run(){
		var medias = mediaRepository.findAllByAvailability(MediaAvailability.DOWNLOADING);
		for(var media : medias){
			try{
				mediaService.update(media);
			}
			catch(UpdateException | RequestFailedException e){
				log.error("Failed to update media {}", media, e);
			}
		}
		
		log.info("Done updating {} media", medias.size());
	}
}
