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
	private final String plexEndpoint;
	private final String plexServerId;
	private final String applicationEndpoint;
	
	@Autowired
	public ThymeleafService(ApplicationConfiguration applicationConfiguration){
		this.overseerrEndpoint = applicationConfiguration.getOverseerr().getEndpoint();
		this.radarrEndpoint = applicationConfiguration.getRadarr().getEndpoint();
		this.sonarrEndpoint = applicationConfiguration.getSonarr().getEndpoint();
		this.plexEndpoint = applicationConfiguration.getPlex().getAppEndpoint();
		this.plexServerId = applicationConfiguration.getPlex().getServerId();
		this.applicationEndpoint = applicationConfiguration.getApplicationUrl();
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
	public String getMediaPlexUrl(@NotNull MediaEntity media){
		return Optional.ofNullable(media.getPlexId())
				.map(this::getRatingKeyPlexUrl)
				.orElse(null);
	}
	
	@Nullable
	public String getRatingKeyPlexUrl(int ratingKey){
		return "%s/desktop/#!/server/%s/details?key=%%2Flibrary%%2Fmetadata%%2F%d".formatted(plexEndpoint, plexServerId, ratingKey);
	}
	
	@Nullable
	public String getMediaTmdbUrl(@NotNull MediaEntity media){
		var type = switch(media.getType()){
			case MOVIE -> "movie";
			case SEASON, EPISODE -> "tv";
		};
		
		return Optional.ofNullable(media.getTmdbId())
				.map(id -> "https://www.themoviedb.org/%s/%d".formatted(type, id))
				.orElse(null);
	}
	
	@Nullable
	public String getMediaTvdbUrl(@NotNull MediaEntity media){
		var type = switch(media.getType()){
			case MOVIE -> "movie";
			case SEASON, EPISODE -> "series";
		};
		
		return Optional.ofNullable(media.getTvdbId())
				.map(slug -> "https://www.thetvdb.com/dereferrer/%s/%s".formatted(type, slug))
				.orElse(null);
	}
	
	@Nullable
	public String getOwnUrl(){
		return applicationEndpoint;
	}
	
	@NotNull
	public String getAddWatchMediaUrl(int mediaId){
		return "%s/api/user/media-requirement/add?media=%d".formatted(getOwnUrl(), mediaId);
	}
	
	@Nullable
	public String getTableColorClass(@NotNull MediaEntity media){
		return media.getStatus().getTableClass();
	}
}
