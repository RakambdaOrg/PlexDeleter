package fr.rakambda.plexdeleter.config;

import jakarta.validation.constraints.NotBlank;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import java.util.List;

@Validated
@ConfigurationProperties("app.plex")
public record PlexConfiguration(
		@NonNull @NotBlank String endpoint,
		@NonNull @NotBlank String appEndpoint,
		@NonNull @NotBlank String communityEndpoint,
		@NonNull @NotBlank String communityToken,
		@NonNull @NotBlank String pmsEndpoint,
		@NonNull @NotBlank String pmsToken,
		@NonNull @NotBlank String serverId,
		@Nullable List<Integer> temporaryLibraries
){
}
