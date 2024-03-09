package fr.rakambda.plexdeleter.web.user;

import fr.rakambda.plexdeleter.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController{
	private final UserService userService;
	
	@Autowired
	public UserController(UserService userService){
		this.userService = userService;
	}
	
	@GetMapping(value = "/home")
	public ModelAndView index(){
		var mav = new ModelAndView("user/index");
		mav.addObject("userService", userService);
		return mav;
	}
}
