package fr.rakambda.plexdeleter.api.plex.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterReflectionForBinding(User.class)
public class User{
	@Nullable
	private Integer id;
	@NotNull
	private String username;
	@NotNull
	private String uuid;
	@Nullable
	private String title;
	@NotNull
	private String email;
	@Nullable
	private String friendlyName;
}
