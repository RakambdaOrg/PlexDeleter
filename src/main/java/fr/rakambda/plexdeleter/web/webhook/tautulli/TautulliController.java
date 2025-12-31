package fr.rakambda.plexdeleter.web.webhook.tautulli;

import fr.rakambda.plexdeleter.api.RequestFailedException;
import fr.rakambda.plexdeleter.notify.NotifyException;
import fr.rakambda.plexdeleter.service.TautulliService;
import fr.rakambda.plexdeleter.service.UpdateException;
import fr.rakambda.plexdeleter.web.api.ThymeleafMessageException;
import fr.rakambda.plexdeleter.web.webhook.tautulli.data.TautulliWebhook;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/webhook/tautulli")
public class TautulliController{
	private final TautulliService tautulliService;
	
	public TautulliController(TautulliService tautulliService){
		this.tautulliService = tautulliService;
	}
	
	@Transactional
	@PostMapping
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void onCall(@NonNull @RequestBody TautulliWebhook data) throws RequestFailedException, IOException, UpdateException, NotifyException, ThymeleafMessageException{
		log.info("Received new Tautulli webhook {}", data);
		
		if(!data.getMediaType().isNotifyAdded()){
			return;
		}
		
		switch(data.getType()){
			case "watched" -> {
				tautulliService.addNewMediaIfPreviousExist(data);
				tautulliService.updateRequirement(data);
			}
			case "added" -> {
				tautulliService.addNewMediaIfPreviousExist(data);
				tautulliService.updateMedia(data);
				tautulliService.notifyMedia(data);
			}
		}
	}
}
