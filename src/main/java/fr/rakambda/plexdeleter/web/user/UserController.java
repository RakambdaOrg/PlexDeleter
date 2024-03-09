package fr.rakambda.plexdeleter.web.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController{
	@GetMapping(value = "/home")
	public ModelAndView index(){
		return new ModelAndView("user/index");
	}
}
