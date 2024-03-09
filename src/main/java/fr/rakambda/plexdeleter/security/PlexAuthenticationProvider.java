package fr.rakambda.plexdeleter.security;

import fr.rakambda.plexdeleter.api.plex.PlexApiService;
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
		
		return authenticateAgainstThirdPartyAndGetAuthentication(
				plexAuthenticationToken.getName(),
				plexAuthenticationToken.getCredentials().toString(),
				plexAuthenticationToken.getOtp()
		);
	}
	
	private UsernamePasswordAuthenticationToken authenticateAgainstThirdPartyAndGetAuthentication(@NotNull String username, @NotNull String password, @Nullable String otp){
		try{
			var result = plexApiService.authenticate(username, password, otp);
			var userDetails = userDetailsService.loadUserByUsername(String.valueOf(result.getId()));
			return UsernamePasswordAuthenticationToken.authenticated(new PlexUser(result.getId(), username, password, userDetails.getAuthorities()), password, userDetails.getAuthorities());
		}
		catch(UsernameNotFoundException e){
			throw e;
		}
		catch(Exception e){
			throw new BadCredentialsException("Failed to authenticate with Plex", e);
		}
	}
	
	@Override
	public boolean supports(Class<?> authentication){
		return PlexAuthenticationToken.class.isAssignableFrom(authentication);
	}
}
