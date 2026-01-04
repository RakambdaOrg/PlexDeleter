package fr.rakambda.plexdeleter.schedule;

import fr.rakambda.plexdeleter.api.RequestFailedException;
import fr.rakambda.plexdeleter.service.MediaService;
import fr.rakambda.plexdeleter.storage.entity.MediaStatus;
import fr.rakambda.plexdeleter.storage.repository.MediaRepository;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class UpdateMediaCollectionsScheduler implements IScheduler{
	private final MediaRepository mediaRepository;
	private final MediaService mediaService;
	
	@Autowired
	public UpdateMediaCollectionsScheduler(MediaRepository mediaRepository, MediaService mediaService){
		this.mediaRepository = mediaRepository;
		this.mediaService = mediaService;
	}
	
	@Override
	@NonNull
	public String getTaskId(){
		return "media-collections-update";
	}
	
	@Override
	@Scheduled(cron = "0 45 0,8,15 * * *")
	@Transactional
	public void run(){
		log.info("Updating media collections");
		var medias = mediaRepository.findAllByStatusIn(MediaStatus.allOnDisk());
		
		for(var media : medias){
			try{
				mediaService.updateCollections(media);
			}
			catch(RequestFailedException e){
				log.error("Failed to update media collections {}", media, e);
			}
		}
		
		log.info("Done updating {} media collections", medias.size());
	}
}
