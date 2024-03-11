package fr.rakambda.plexdeleter.api.overseerr.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterReflectionForBinding(User.class)
public class User{
	private int permissions;
	private int id;
	@NotNull
	private String email;
	@NotNull
	private String plexUsername;
	@NotNull
	private String username;
	@NotNull
	private String displayName;
	private int userType;
	private int plexId;
	@Nullable
	private String avatar;
	@NotNull
	private Instant createdAt;
	@NotNull
	private Instant updatedAt;
	private int requestCount;
	@NotNull
	private Settings settings;
}
