package fr.rakambda.plexdeleter.web.webhook.radarr;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/webhook/radarr")
public class RadarrController{
	@PostMapping
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void onCall(){
	}
}
