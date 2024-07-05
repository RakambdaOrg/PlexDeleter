package fr.rakambda.plexdeleter.web;

import fr.rakambda.plexdeleter.web.api.ThymeleafMessageException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class ExceptionControllerAdvice{
	@ExceptionHandler(Exception.class)
	public ModelAndView handle(Exception e){
		return switch(e){
			case ThymeleafMessageException thymeleafMessageException -> {
				var mav = new ModelAndView("api/errorLocalized");
				mav.addObject("expression", thymeleafMessageException.getExpression());
				yield mav;
			}
			default -> {
				var mav = new ModelAndView("api/error");
				mav.addObject("errorMessage", e.getMessage());
				yield mav;
			}
		};
	}
}
