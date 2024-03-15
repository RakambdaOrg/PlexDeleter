package fr.rakambda.plexdeleter.schedule;

import fr.rakambda.plexdeleter.api.RequestFailedException;
import fr.rakambda.plexdeleter.notify.NotifyException;
import fr.rakambda.plexdeleter.service.MediaService;
import fr.rakambda.plexdeleter.service.UpdateException;
import fr.rakambda.plexdeleter.storage.entity.MediaAvailability;
import fr.rakambda.plexdeleter.storage.repository.MediaRepository;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.Set;

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
	@Transactional
	public void run(){
		log.info("Updating medias");
		var medias = mediaRepository.findAllByAvailabilityIn(Set.of(MediaAvailability.WAITING, MediaAvailability.DOWNLOADING));
		for(var media : medias){
			try{
				mediaService.update(media);
			}
			catch(UpdateException | RequestFailedException | NotifyException e){
				log.error("Failed to update media {}", media, e);
			}
		}
		
		log.info("Done updating {} media", medias.size());
	}
}
