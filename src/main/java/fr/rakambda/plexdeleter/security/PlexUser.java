package fr.rakambda.plexdeleter.security;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import java.io.Serial;
import java.util.Collection;

@Getter
public class PlexUser extends User{
	@Serial
	private static final long serialVersionUID = -3173433327282884371L;
	
	private final int plexId;
	
	public PlexUser(int plexId, @NotNull String username, @Nullable String password, @NotNull Collection<? extends GrantedAuthority> authorities){
		super(username, password, authorities);
		this.plexId = plexId;
	}
}
