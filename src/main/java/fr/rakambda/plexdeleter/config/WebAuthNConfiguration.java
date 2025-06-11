package fr.rakambda.plexdeleter.config;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.lang.NonNull;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebAuthNConfiguration{
	@NotNull
	@NonNull
	private String relayingPartyName;
	@NotNull
	@NonNull
	private String relayingPartyId;
	@NotNull
	@NotEmpty
	private Set<String> allowedOrigins;
}
