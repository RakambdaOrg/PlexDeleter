package fr.rakambda.plexdeleter.schedule;

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
public class UpdateMediaLabelsScheduler implements IScheduler{
	private final MediaRepository mediaRepository;
	private final MediaService mediaService;
	
	@Autowired
	public UpdateMediaLabelsScheduler(MediaRepository mediaRepository, MediaService mediaService){
		this.mediaRepository = mediaRepository;
		this.mediaService = mediaService;
	}
	
	@Override
	@NonNull
	public String getTaskId(){
		return "media-labels-update";
	}
	
	@Override
	@Scheduled(cron = "0 45 0,8,15 * * *")
	@Transactional
	public void run(){
		log.info("Updating media labels");
		var medias = mediaRepository.findAllByStatusIn(MediaStatus.allOnDisk());
		
		for(var media : medias){
			try{
				mediaService.updateMediaLabels(media);
			}
			catch(Exception e){
				log.error("Failed to update media labels {}", media, e);
			}
		}
		
		log.info("Done updating {} media labels", medias.size());
	}
}
