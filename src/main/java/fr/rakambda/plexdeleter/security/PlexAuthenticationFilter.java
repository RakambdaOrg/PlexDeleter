package fr.rakambda.plexdeleter.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import java.util.Optional;

public class PlexAuthenticationFilter extends UsernamePasswordAuthenticationFilter{
	private static final String ID_PARAMETER = "id";
	private static final String CODE_PARAMETER = "code";
	
	public PlexAuthenticationFilter(){
		super();
	}
	
	@Override
	public Authentication attemptAuthentication(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response) throws AuthenticationException{
		if(!request.getMethod().equals("POST")){
			throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
		}
		
		var id = Optional.ofNullable(request.getParameter(ID_PARAMETER)).map(String::trim).map(Long::parseLong).orElse(null);
		
		var authRequest = PlexAuthenticationToken.unauthenticated(id);
		authRequest.setDetails(this.authenticationDetailsSource.buildDetails(request));
		return getAuthenticationManager().authenticate(authRequest);
	}
}
