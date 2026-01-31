package fr.rakambda.plexdeleter.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NonNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TautulliConfiguration{
	@NonNull
	private String endpoint;
	@NonNull
	private String apiKey;
}
