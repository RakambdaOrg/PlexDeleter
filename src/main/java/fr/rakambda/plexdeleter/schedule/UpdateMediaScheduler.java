package fr.rakambda.plexdeleter.schedule;

import fr.rakambda.plexdeleter.service.MediaService;
import fr.rakambda.plexdeleter.storage.repository.MediaRepository;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
	@NonNull
	public String getTaskId(){
		return "media-update";
	}
	
	@Override
	@Scheduled(cron = "0 0 0,8,15 * * *")
	@Transactional
	public void run(){
		mediaService.updateAll();
	}
}
