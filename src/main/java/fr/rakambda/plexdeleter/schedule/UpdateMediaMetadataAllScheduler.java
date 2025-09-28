package fr.rakambda.plexdeleter.schedule;

import fr.rakambda.plexdeleter.service.MediaService;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class UpdateMediaMetadataAllScheduler implements IScheduler{
	private final MediaService mediaService;
	
	@Autowired
	public UpdateMediaMetadataAllScheduler(MediaService mediaService){
		this.mediaService = mediaService;
	}
	
	@Override
	@NonNull
	public String getTaskId(){
		return "media-update-all-metadata";
	}
	
	@Override
	@Scheduled(cron = "-")
	@Transactional
	public void run(){
		mediaService.updateAllMetadata();
	}
}
