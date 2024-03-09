package fr.rakambda.plexdeleter.security;

import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.security.config.annotation.web.configurers.AbstractAuthenticationFilterConfigurer;
import org.springframework.security.web.authentication.ForwardAuthenticationFailureHandler;
import org.springframework.security.web.authentication.ForwardAuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

public class PlexFormLoginConfigurer<H extends HttpSecurityBuilder<H>> extends AbstractAuthenticationFilterConfigurer<H, PlexFormLoginConfigurer<H>, PlexAuthenticationFilter>{
	
	public PlexFormLoginConfigurer<H> authenticationFilter(PlexAuthenticationFilter authenticationFilter){
		super.setAuthenticationFilter(authenticationFilter);
		return this;
	}
	
	@Override
	protected RequestMatcher createLoginProcessingUrlMatcher(String loginProcessingUrl){
		return new AntPathRequestMatcher(loginProcessingUrl, "POST");
	}
	
	@Override
	public PlexFormLoginConfigurer<H> loginPage(String loginPage){
		return super.loginPage(loginPage);
	}
	
	public PlexFormLoginConfigurer<H> failureForwardUrl(String forwardUrl){
		failureHandler(new ForwardAuthenticationFailureHandler(forwardUrl));
		return this;
	}
	
	public PlexFormLoginConfigurer<H> successForwardUrl(String forwardUrl){
		successHandler(new ForwardAuthenticationSuccessHandler(forwardUrl));
		return this;
	}
}
