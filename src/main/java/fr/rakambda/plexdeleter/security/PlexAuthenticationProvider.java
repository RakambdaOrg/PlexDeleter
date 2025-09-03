package fr.rakambda.plexdeleter.security;

import fr.rakambda.plexdeleter.api.plex.PlexApiService;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import java.util.Objects;

@Slf4j
@Component
public class PlexAuthenticationProvider implements AuthenticationProvider{
	private final PlexApiService plexApiService;
	private final UserDetailsService userDetailsService;
	
	@Autowired
	public PlexAuthenticationProvider(PlexApiService plexApiService, UserDetailsService userDetailsService){
		this.plexApiService = plexApiService;
		this.userDetailsService = userDetailsService;
	}
	
	@Override
	public Authentication authenticate(@Nullable Authentication authentication) throws AuthenticationException{
		if(!(authentication instanceof PlexAuthenticationToken plexAuthenticationToken)){
			return authentication;
		}
		log.info("Processing Plex authentication");
		var authToken = plexAuthenticationToken.getAuthToken();
		
		return authenticateAgainstThirdPartyAndGetAuthentication(authToken);
	}
	
	@NonNull
	private UsernamePasswordAuthenticationToken authenticateAgainstThirdPartyAndGetAuthentication(@Nullable String authToken){
		try{
			if(Objects.isNull(authToken)){
				throw new BadCredentialsException("Plex authentication token is null");
			}
			var userInfo = plexApiService.getUserInfo(authToken);
			
			var dbUsername = "plexid_%d".formatted(userInfo.getId());
			var userDetails = userDetailsService.loadUserByUsername(dbUsername);
			var principal = new PlexUser(userInfo.getId(), userInfo.getUsername(), authToken, userDetails.getAuthorities());
			
			log.info("Got principal {}", principal);
			return UsernamePasswordAuthenticationToken.authenticated(principal, authToken, userDetails.getAuthorities());
		}
		catch(UsernameNotFoundException e){
			log.error("Failed to get user from database", e);
			throw e;
		}
		catch(Exception e){
			log.error("Failed to authenticate with Plex", e);
			throw new BadCredentialsException("Failed to authenticate with Plex", e);
		}
	}
	
	@Override
	public boolean supports(@NonNull Class<?> authentication){
		return PlexAuthenticationToken.class.isAssignableFrom(authentication);
	}
}
