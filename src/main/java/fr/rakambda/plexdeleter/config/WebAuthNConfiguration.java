package fr.rakambda.plexdeleter.config;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NonNull;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebAuthNConfiguration{
	@NonNull
	private String relayingPartyName;
	@NonNull
	private String relayingPartyId;
	@NonNull
	@NotEmpty
	private Set<String> allowedOrigins;
}
