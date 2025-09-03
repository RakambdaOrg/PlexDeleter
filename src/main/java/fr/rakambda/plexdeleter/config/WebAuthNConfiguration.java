package fr.rakambda.plexdeleter.config;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebAuthNConfiguration{
	@NonNull
	@org.jspecify.annotations.NonNull
	private String relayingPartyName;
	@NonNull
	@org.jspecify.annotations.NonNull
	private String relayingPartyId;
	@org.jspecify.annotations.NonNull
	@NotEmpty
	private Set<String> allowedOrigins;
}
