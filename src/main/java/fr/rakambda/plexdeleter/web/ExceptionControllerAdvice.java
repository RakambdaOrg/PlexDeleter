package fr.rakambda.plexdeleter.web;

import fr.rakambda.plexdeleter.web.api.ThymeleafMessageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import java.io.PrintWriter;
import java.io.StringWriter;

@Slf4j
@ControllerAdvice
public class ExceptionControllerAdvice{
	@ExceptionHandler(Exception.class)
	public ModelAndView handle(Exception e){
		log.error("Caught exception in a controller", e);
		
		var modelAndView = switch(e){
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
		
		var traceWriter = new StringWriter();
		try(var printWriter = new PrintWriter(traceWriter)){
			e.printStackTrace(printWriter);
		}
		
		modelAndView.addObject("errorTrace", traceWriter.toString());
		return modelAndView;
	}
}
