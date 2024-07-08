package fr.rakambda.plexdeleter.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import java.util.Objects;
import java.util.Optional;

public class RefererModelInterceptor implements HandlerInterceptor{
	@Override
	public void postHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler, @Nullable ModelAndView modelAndView){
		if(Objects.isNull(modelAndView)){
			return;
		}
		modelAndView.addObject("referer", getPreviousPageByRequest(request).orElse(null));
	}
	
	@NotNull
	private Optional<String> getPreviousPageByRequest(@NotNull HttpServletRequest request){
		return Optional.ofNullable(request.getHeader("Referer"));
	}
}
