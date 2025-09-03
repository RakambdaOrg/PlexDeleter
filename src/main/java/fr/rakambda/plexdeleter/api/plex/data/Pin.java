package fr.rakambda.plexdeleter.api.plex.data;

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
@RegisterReflectionForBinding(Pin.class)
public class Pin{
	private long id;
	@NonNull
	private String code;
	@Nullable
	private String product;
	private boolean trusted;
	@Nullable
	private String qr;
	@Nullable
	private String clientIdentifier;
	private int expiresIn;
	@Nullable
	private Instant createdAt;
	@Nullable
	private Instant expiresAt;
	@Nullable
	private String authToken;
	@Nullable
	private String newRegistration;
}
