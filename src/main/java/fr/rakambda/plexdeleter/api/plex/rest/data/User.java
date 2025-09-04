package fr.rakambda.plexdeleter.api.plex.rest.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterReflectionForBinding(User.class)
public class User{
	@Nullable
	private Integer id;
	@NonNull
	private String username;
	@NonNull
	private String uuid;
	@Nullable
	private String title;
	@NonNull
	private String email;
	@Nullable
	private String friendlyName;
}
