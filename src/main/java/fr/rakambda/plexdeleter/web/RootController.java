package fr.rakambda.plexdeleter.web;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@Slf4j
@RestController
@RequestMapping("/")
public class RootController{
	@RequestMapping(value = {
			"/robots.txt",
			"/robot.txt"
	})
	@ResponseBody
	@NotNull
	public String getRobotsTxt(){
		return """
				User-agent: *
				Disallow: /""";
	}
	
	@GetMapping
	public ModelAndView getRoot(){
		return new ModelAndView("redirect:/user/home");
	}
}
