package fr.rakambda.plexdeleter.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.lang.NonNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SonarrConfiguration{
	@NotNull
	@NonNull
	private String endpoint;
	@NotNull
	@NonNull
	private String apiKey;
}
