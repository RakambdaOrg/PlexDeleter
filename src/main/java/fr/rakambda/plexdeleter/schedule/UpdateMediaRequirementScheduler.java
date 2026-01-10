package fr.rakambda.plexdeleter.schedule;

import fr.rakambda.plexdeleter.api.RequestFailedException;
import fr.rakambda.plexdeleter.notify.NotifyException;
import fr.rakambda.plexdeleter.service.WatchService;
import fr.rakambda.plexdeleter.storage.entity.MediaRequirementStatus;
import fr.rakambda.plexdeleter.storage.repository.MediaRequirementRepository;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.io.IOException;

@Slf4j
@Component
public class UpdateMediaRequirementScheduler implements IScheduler{
	private final MediaRequirementRepository mediaRequirementRepository;
	private final WatchService watchService;
	
	@Autowired
	public UpdateMediaRequirementScheduler(MediaRequirementRepository mediaRequirementRepository, WatchService watchService){
		this.mediaRequirementRepository = mediaRequirementRepository;
		this.watchService = watchService;
	}
	
	@Override
	@NonNull
	public String getTaskId(){
		return "media-requirement-update";
	}
	
	@Override
	@Scheduled(cron = "0 30 0,8,15 * * *")
	@Transactional
	public void run(){
		log.info("Updating media requirements");
		var requirements = mediaRequirementRepository.findAllByStatusIs(MediaRequirementStatus.WAITING);
		for(var requirement : requirements){
			try{
				watchService.update(requirement);
			}
			catch(RequestFailedException | IOException | NotifyException e){
				log.error("Failed to update media requirement {}", requirement, e);
			}
		}
		
		log.info("Done updating {} media requirements", requirements.size());
	}
}
