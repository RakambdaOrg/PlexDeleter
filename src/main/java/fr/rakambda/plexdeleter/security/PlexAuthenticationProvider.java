package fr.rakambda.plexdeleter.security;

import fr.rakambda.plexdeleter.api.plex.PlexApiService;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
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
		log.info("Processing Plex authentication with {}", authentication.getName());
		
		return authenticateAgainstThirdPartyAndGetAuthentication(
				plexAuthenticationToken.getName(),
				plexAuthenticationToken.getCredentials().toString(),
				plexAuthenticationToken.getOtp()
		);
	}
	
	private UsernamePasswordAuthenticationToken authenticateAgainstThirdPartyAndGetAuthentication(@NotNull String username, @NotNull String password, @Nullable String otp){
		try{
			var result = plexApiService.authenticate(username, password, otp);
			log.info("Successfully authenticated with Plex: {}", result.getId());
			
			var dbUsername = "plexid_%d".formatted(result.getId());
			var userDetails = userDetailsService.loadUserByUsername(dbUsername);
			var principal = new PlexUser(result.getId(), username, password, userDetails.getAuthorities());
			
			log.info("Got principal {}", principal);
			return UsernamePasswordAuthenticationToken.authenticated(principal, password, userDetails.getAuthorities());
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
