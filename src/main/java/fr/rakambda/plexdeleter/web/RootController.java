package fr.rakambda.plexdeleter.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@Slf4j
@RestController
@RequestMapping("/")
public class RootController{
	@GetMapping
	public ModelAndView getRoot(){
		return new ModelAndView("redirect:/user/home");
	}
}
