package fr.rakambda.plexdeleter.security;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import java.util.Collection;

@Getter
public class PlexUser extends User{
	private final int plexId;
	
	public PlexUser(int plexId, @NotNull String username, @Nullable String password, @NotNull Collection<? extends GrantedAuthority> authorities){
		super(username, password, authorities);
		this.plexId = plexId;
	}
}
