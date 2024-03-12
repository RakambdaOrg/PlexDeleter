package fr.rakambda.plexdeleter.web.auth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController{
	@RequestMapping
	public ModelAndView plexLogin() {
		var mav = new ModelAndView("auth/plex");
		mav.addObject("appId", "PlexDeleter");
		mav.addObject("clientIdLocalStorage", "PlexDeleterClientId");
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
