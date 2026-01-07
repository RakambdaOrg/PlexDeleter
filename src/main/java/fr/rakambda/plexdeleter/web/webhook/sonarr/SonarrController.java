package fr.rakambda.plexdeleter.web.webhook.sonarr;

import fr.rakambda.plexdeleter.service.SonarrService;
import fr.rakambda.plexdeleter.web.webhook.sonarr.data.SonarrWebhook;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/webhook/sonarr")
public class SonarrController{
	private final SonarrService sonarrService;
	
	public SonarrController(SonarrService sonarrService){
		this.sonarrService = sonarrService;
	}
	
	@PostMapping
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void onCall(@NonNull @RequestBody SonarrWebhook data){
		log.info("Received new Sonarr webhook {}", data);
		switch(data.getEventType()){
			case "Grab" -> sonarrService.onEpisodeGrabbed(data);
		}
	}
}
