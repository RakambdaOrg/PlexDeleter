package fr.rakambda.plexdeleter.web.auth;

import fr.rakambda.plexdeleter.api.RequestFailedException;
import fr.rakambda.plexdeleter.api.plex.PlexApiService;
import fr.rakambda.plexdeleter.config.ApplicationConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController{
	private final PlexApiService plexApiService;
	private final String appPlexEndpoint;
	
	@Autowired
	public AuthController(PlexApiService plexApiService, ApplicationConfiguration applicationConfiguration){
		this.plexApiService = plexApiService;
		this.appPlexEndpoint = applicationConfiguration.getPlex().getAppEndpoint();
	}
	
	@RequestMapping
	public ModelAndView plexLogin() throws RequestFailedException{
		log.info("New login request");
		var pin = plexApiService.generatePin();
		
		var mav = new ModelAndView("auth/plex");
		mav.addObject("appId", "PlexDeleter");
		mav.addObject("clientIdLocalStorage", "PlexDeleterClientId");
		mav.addObject("pinId", pin.getId());
		mav.addObject("authLink", "%s/auth/#!?code=%s&clientID=%s".formatted(appPlexEndpoint, pin.getCode(), pin.getClientIdentifier()));
		return mav;
	}
	
	@RequestMapping(value = "/success")
	public ModelAndView success(){
		return new ModelAndView("auth/success");
	}
	
	@RequestMapping(value = "/error")
	public ModelAndView error(){
		return new ModelAndView("auth/error");
	}
}
