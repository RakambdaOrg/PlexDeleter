package fr.rakambda.plexdeleter.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import java.util.Collection;

@Getter
public class PlexUser extends User{
	private final int plexId;
	
	public PlexUser(Integer plexId, String username, String password, Collection<? extends GrantedAuthority> authorities){
		super(username, password, authorities);
		this.plexId = plexId;
	}
}
