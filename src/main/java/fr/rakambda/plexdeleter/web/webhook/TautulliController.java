package fr.rakambda.plexdeleter.web.webhook;

import fr.rakambda.plexdeleter.api.RequestFailedException;
import fr.rakambda.plexdeleter.service.MediaService;
import fr.rakambda.plexdeleter.service.UpdateException;
import fr.rakambda.plexdeleter.service.WatchService;
import org.jetbrains.annotations.NotNull;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.io.IOException;

@RestController
@RequestMapping("/webhook/tautulli")
public class TautulliController{
	private final WatchService watchService;
	private final MediaService mediaService;
	
	public TautulliController(WatchService watchService, MediaService mediaService){
		this.watchService = watchService;
		this.mediaService = mediaService;
	}
	
	@PostMapping("/")
	public void onCall(@NonNull TautulliWebhook data) throws RequestFailedException, IOException, UpdateException{
		switch(data.getType()){
			case "watched" -> updateRequirement(data);
			case "added" -> updateMedia(data);
		}
		;
	}
	
	private void updateRequirement(@NotNull TautulliWebhook data) throws RequestFailedException, IOException{
		// watchService.update(null); //TODO
	}
	
	private void updateMedia(TautulliWebhook data) throws RequestFailedException, UpdateException{
		// mediaService.update(null); //TODO
	}
}
