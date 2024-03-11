package fr.rakambda.plexdeleter.security;

import fr.rakambda.plexdeleter.api.plex.PlexApiService;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
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
		var id = plexAuthenticationToken.getPrincipal();
		log.info("Processing Plex authentication with id {}", id);
		
		return authenticateAgainstThirdPartyAndGetAuthentication(id);
	}
	
	private UsernamePasswordAuthenticationToken authenticateAgainstThirdPartyAndGetAuthentication(long id){
		try{
			var result = plexApiService.pollAuthToken(id);
			log.info("Successfully authenticated on Plex with pin {}", result.getId());
			
			var authToken = result.getAuthToken();
			if(Objects.isNull(authToken)){
				throw new BadCredentialsException("Could not get auth token from Plex");
			}
			var userInfo = plexApiService.getUserInfo(result.getAuthToken());
			
			var dbUsername = "plexid_%d".formatted(userInfo.getId());
			var userDetails = userDetailsService.loadUserByUsername(dbUsername);
			var principal = new PlexUser(userInfo.getId(), userInfo.getUsername(), authToken, userDetails.getAuthorities());
			
			log.info("Got principal {} from id {}", principal, id);
			return UsernamePasswordAuthenticationToken.authenticated(principal, id, userDetails.getAuthorities());
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
	public boolean supports(Class<?> authentication){
		return PlexAuthenticationToken.class.isAssignableFrom(authentication);
	}
}
