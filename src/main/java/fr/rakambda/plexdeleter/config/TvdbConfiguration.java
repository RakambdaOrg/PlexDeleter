package fr.rakambda.plexdeleter.config;

import jakarta.validation.constraints.NotBlank;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("app.tvdb")
public record TvdbConfiguration(
		@NonNull @NotBlank String endpoint,
		@NonNull @NotBlank String apiKey
){
}
