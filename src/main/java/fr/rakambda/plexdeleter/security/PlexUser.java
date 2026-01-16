package fr.rakambda.plexdeleter.security;

import lombok.Getter;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import java.io.Serial;
import java.util.Collection;

@Getter
@RegisterReflectionForBinding(PlexAuthenticationToken.class)
public class PlexUser extends User{
	@Serial
	private static final long serialVersionUID = -3173433327282884371L;
	
	private final int plexId;
	
	public PlexUser(int plexId, @NonNull String username, @Nullable String password, @NonNull Collection<? extends GrantedAuthority> authorities){
		super(username, password, authorities);
		this.plexId = plexId;
	}
}
