package fr.rakambda.plexdeleter.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.context.annotation.Configuration;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Configuration
@ConfigurationProperties(prefix = "app")
public class ApplicationConfiguration{
	@NestedConfigurationProperty
	private OverseerrConfiguration overseerr;
	@NestedConfigurationProperty
	private TautulliConfiguration tautulli;
	@NestedConfigurationProperty
	private SonarrConfiguration sonarr;
	@NestedConfigurationProperty
	private RadarrConfiguration radarr;
	@NestedConfigurationProperty
	private DeletionConfiguration deletion;
}
