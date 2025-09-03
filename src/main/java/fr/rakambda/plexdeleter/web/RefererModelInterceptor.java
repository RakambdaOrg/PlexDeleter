package fr.rakambda.plexdeleter.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import java.util.Objects;
import java.util.Optional;

public class RefererModelInterceptor implements HandlerInterceptor{
	@Override
	public void postHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, @Nullable ModelAndView modelAndView){
		if(Objects.isNull(modelAndView)){
			return;
		}
		modelAndView.addObject("referer", getPreviousPageByRequest(request).orElse(null));
	}
	
	@NonNull
	private Optional<String> getPreviousPageByRequest(@NonNull HttpServletRequest request){
		return Optional.ofNullable(request.getHeader("Referer"));
	}
}
