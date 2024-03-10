package fr.rakambda.plexdeleter.security;

import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;

@Getter
public class PlexAuthenticationToken extends AbstractAuthenticationToken{
	private final Long principal;
	private String credentials;
	
	public static PlexAuthenticationToken unauthenticated(Long principal){
		return new PlexAuthenticationToken(principal, null);
	}
	
	private PlexAuthenticationToken(Long principal, String credentials){
		super(null);
		this.principal = principal;
		this.credentials = credentials;
	}
	
	@Override
	public void eraseCredentials(){
		super.eraseCredentials();
		this.credentials = null;
	}
}
