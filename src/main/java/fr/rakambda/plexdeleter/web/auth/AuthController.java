package fr.rakambda.plexdeleter.web.auth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController{
	@RequestMapping
	public ModelAndView plexLogin(){
		return new ModelAndView("auth/plex");
	}
	
	@RequestMapping(value = "/success", method = {
			RequestMethod.GET,
			RequestMethod.POST
	})
	public ModelAndView success(){
		return new ModelAndView("redirect:user/home");
	}
}
