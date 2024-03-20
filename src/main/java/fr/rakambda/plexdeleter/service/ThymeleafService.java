package fr.rakambda.plexdeleter.service;

import fr.rakambda.plexdeleter.config.ApplicationConfiguration;
import fr.rakambda.plexdeleter.storage.entity.MediaEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class ThymeleafService{
	private final String overseerrEndpoint;
	private final String radarrEndpoint;
	private final String sonarrEndpoint;
	
	@Autowired
	public ThymeleafService(ApplicationConfiguration applicationConfiguration){
		this.overseerrEndpoint = applicationConfiguration.getOverseerr().getEndpoint();
		this.radarrEndpoint = applicationConfiguration.getRadarr().getEndpoint();
		this.sonarrEndpoint = applicationConfiguration.getSonarr().getEndpoint();
	}
	
	@Nullable
	public String getMediaOverseerrUrl(@NotNull MediaEntity media){
		return Optional.ofNullable(media.getOverseerrId())
				.map(id -> "%s/%s/%d".formatted(overseerrEndpoint, media.getType().getOverseerrType().getValue(), id))
				.orElse(null);
	}
	
	@Nullable
	public String getMediaSonarrUrl(@NotNull MediaEntity media){
		return Optional.ofNullable(media.getSonarrSlug())
				.map(id -> "%s/series/%s".formatted(sonarrEndpoint, id))
				.orElse(null);
	}
	
	@Nullable
	public String getMediaRadarrUrl(@NotNull MediaEntity media){
		return Optional.ofNullable(media.getRadarrSlug())
				.map(id -> "%s/movie/%s".formatted(radarrEndpoint, id))
				.orElse(null);
	}
	
	@Nullable
	public String getTableColorClass(@NotNull MediaEntity media){
		return media.getAvailability().getTableClass();
	}
}
