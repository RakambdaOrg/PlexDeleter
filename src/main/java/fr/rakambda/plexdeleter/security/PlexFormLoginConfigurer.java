package fr.rakambda.plexdeleter.security;

import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.security.config.annotation.web.configurers.AbstractAuthenticationFilterConfigurer;
import org.springframework.security.web.authentication.ForwardAuthenticationFailureHandler;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

public class PlexFormLoginConfigurer<H extends HttpSecurityBuilder<H>> extends AbstractAuthenticationFilterConfigurer<H, PlexFormLoginConfigurer<H>, PlexAuthenticationFilter>{
	@NotNull
	public PlexFormLoginConfigurer<H> authenticationFilter(@NotNull PlexAuthenticationFilter authenticationFilter){
		super.setAuthenticationFilter(authenticationFilter);
		return this;
	}
	
	@NotNull
	@Override
	protected RequestMatcher createLoginProcessingUrlMatcher(@NotNull String loginProcessingUrl){
		return PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.POST, loginProcessingUrl);
	}
	
	@NotNull
	@Override
	public PlexFormLoginConfigurer<H> loginPage(@NotNull String loginPage){
		return super.loginPage(loginPage);
	}
	
	@NotNull
	public PlexFormLoginConfigurer<H> failureForwardUrl(@NotNull String forwardUrl){
		failureHandler(new ForwardAuthenticationFailureHandler(forwardUrl));
		return this;
	}
}
