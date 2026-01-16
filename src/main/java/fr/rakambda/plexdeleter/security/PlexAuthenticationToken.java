package fr.rakambda.plexdeleter.security;

import lombok.Getter;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import java.io.Serial;

@Getter
@RegisterReflectionForBinding(PlexAuthenticationToken.class)
public class PlexAuthenticationToken extends AbstractAuthenticationToken{
	@Serial
	private static final long serialVersionUID = -6986445552335701701L;
	
	private String authToken;
	
	public static PlexAuthenticationToken unauthenticated(@Nullable String authToken){
		return new PlexAuthenticationToken(authToken);
	}
	
	private PlexAuthenticationToken(@Nullable String authToken){
		super(null);
		this.authToken = authToken;
	}
	
	@Override
	public void eraseCredentials(){
		super.eraseCredentials();
		this.authToken = null;
	}
	
	@Nullable
	@Override
	public Object getCredentials(){
		return authToken;
	}
	
	@Nullable
	@Override
	public Object getPrincipal(){
		return null;
	}
}
