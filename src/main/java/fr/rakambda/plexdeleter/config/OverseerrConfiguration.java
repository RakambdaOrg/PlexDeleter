package fr.rakambda.plexdeleter.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OverseerrConfiguration{
	@NonNull
	@org.jspecify.annotations.NonNull
	private String endpoint;
	@NonNull
	@org.jspecify.annotations.NonNull
	private String apiKey;
}
