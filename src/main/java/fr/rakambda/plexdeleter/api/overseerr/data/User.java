package fr.rakambda.plexdeleter.api.overseerr.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterReflectionForBinding(User.class)
public class User{
	private int permissions;
	private int id;
	@NonNull
	private String email;
	@NonNull
	private String plexUsername;
	@NonNull
	private String username;
	@NonNull
	private String displayName;
	private int userType;
	private int plexId;
	@Nullable
	private String avatar;
	@NonNull
	private Instant createdAt;
	@NonNull
	private Instant updatedAt;
	private int requestCount;
	@NonNull
	private Settings settings;
}
