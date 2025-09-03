package fr.rakambda.plexdeleter.security;

import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.security.config.annotation.web.configurers.AbstractAuthenticationFilterConfigurer;
import org.springframework.security.web.authentication.ForwardAuthenticationFailureHandler;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

public class PlexFormLoginConfigurer<H extends HttpSecurityBuilder<H>> extends AbstractAuthenticationFilterConfigurer<H, PlexFormLoginConfigurer<H>, PlexAuthenticationFilter>{
	@NonNull
	public PlexFormLoginConfigurer<H> authenticationFilter(@NonNull PlexAuthenticationFilter authenticationFilter){
		super.setAuthenticationFilter(authenticationFilter);
		return this;
	}
	
	@NonNull
	@Override
	protected RequestMatcher createLoginProcessingUrlMatcher(@NonNull String loginProcessingUrl){
		return PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.POST, loginProcessingUrl);
	}
	
	@NonNull
	@Override
	public PlexFormLoginConfigurer<H> loginPage(@NonNull String loginPage){
		return super.loginPage(loginPage);
	}
	
	@NonNull
	public PlexFormLoginConfigurer<H> failureForwardUrl(@NonNull String forwardUrl){
		failureHandler(new ForwardAuthenticationFailureHandler(forwardUrl));
		return this;
	}
}
