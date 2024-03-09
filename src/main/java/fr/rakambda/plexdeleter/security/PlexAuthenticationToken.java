package fr.rakambda.plexdeleter.security;

import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;

@Getter
public class PlexAuthenticationToken extends AbstractAuthenticationToken{
	private final Object principal;
	private final String otp;
	private Object credentials;
	
	public static PlexAuthenticationToken unauthenticated(Object principal, Object credentials, String otp){
		return new PlexAuthenticationToken(principal, credentials, otp);
	}
	
	private PlexAuthenticationToken(Object principal, Object credentials, String otp){
		super(null);
		this.principal = principal;
		this.credentials = credentials;
		this.otp = otp;
	}
	
	@Override
	public void eraseCredentials(){
		super.eraseCredentials();
		this.credentials = null;
	}
}
