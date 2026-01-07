package fr.rakambda.plexdeleter.web.webhook.radarr;

import fr.rakambda.plexdeleter.service.RadarrService;
import fr.rakambda.plexdeleter.web.webhook.radarr.data.RadarrWebhook;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/webhook/radarr")
public class RadarrController{
	private final RadarrService radarrService;
	
	@Autowired
	public RadarrController(RadarrService radarrService){
		this.radarrService = radarrService;
	}
	
	@PostMapping
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void onCall(@NotNull @RequestBody RadarrWebhook data){
		log.info("Received new Radarr webhook {}", data);
		switch(data.getEventType()){
			case "Grab" -> radarrService.onMovieGrabbed(data);
		}
	}
}
