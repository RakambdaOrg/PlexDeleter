package fr.rakambda.plexdeleter.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import java.util.Optional;

@Slf4j
public class PlexAuthenticationFilter extends UsernamePasswordAuthenticationFilter{
	private static final String AUTH_TOKEN_PARAMETER = "authToken";
	
	public PlexAuthenticationFilter(){
		super();
	}
	
	@Override
	public Authentication attemptAuthentication(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response) throws AuthenticationException{
		if(!request.getMethod().equals("POST")){
			throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
		}
		
		log.info("Received a new authentication request with");
		var authToken = Optional.ofNullable(request.getParameter(AUTH_TOKEN_PARAMETER)).map(String::trim).orElse(null);
		
		var authRequest = PlexAuthenticationToken.unauthenticated(authToken);
		authRequest.setDetails(this.authenticationDetailsSource.buildDetails(request));
		return getAuthenticationManager().authenticate(authRequest);
	}
}
