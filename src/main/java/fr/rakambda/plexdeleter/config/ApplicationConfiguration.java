package fr.rakambda.plexdeleter.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Configuration
@ConfigurationProperties(prefix = "app")
public class ApplicationConfiguration{
	@NotNull
	@NonNull
	private String applicationUrl;
	@NotNull
	@NonNull
	private String excludeTag;
	@NestedConfigurationProperty
	private PlexConfiguration plex;
	@NestedConfigurationProperty
	private TvdbConfiguration tvdb;
	@NestedConfigurationProperty
	private TmdbConfiguration tmdb;
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
	@NestedConfigurationProperty
	private NotifyWatchlistConfiguration notifyWatchlist;
	@NestedConfigurationProperty
	private MailConfiguration mail;
	@NestedConfigurationProperty
	private SupervisionConfiguration supervision;
	@NestedConfigurationProperty
	private WebAuthNConfiguration webAuthN;
	@NestedConfigurationProperty
	private AmqpConfiguration amqp;
}
