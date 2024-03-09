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
	private static final String USERNAME_PARAMETER = "username";
	private static final String PASSWORD_PARAMETER = "password";
	private static final String OTP_PARAMETER = "otp";
	
	public PlexAuthenticationFilter(){
		super();
	}
	
	@Override
	public Authentication attemptAuthentication(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response) throws AuthenticationException{
		if(!request.getMethod().equals("POST")){
			throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
		}
		
		String username = Optional.ofNullable(request.getParameter(USERNAME_PARAMETER)).map(String::trim).orElse(null);
		String password = Optional.ofNullable(request.getParameter(PASSWORD_PARAMETER)).map(String::trim).orElse(null);
		String otp = Optional.ofNullable(request.getParameter(OTP_PARAMETER)).map(String::trim).orElse(null);
		
		var authRequest = PlexAuthenticationToken.unauthenticated(username, password, otp);
		authRequest.setDetails(this.authenticationDetailsSource.buildDetails(request));
		return getAuthenticationManager().authenticate(authRequest);
	}
}
