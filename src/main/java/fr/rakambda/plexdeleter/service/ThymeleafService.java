package fr.rakambda.plexdeleter.service;

import fr.rakambda.plexdeleter.config.ApplicationConfiguration;
import fr.rakambda.plexdeleter.config.PlexConfiguration;
import fr.rakambda.plexdeleter.config.RadarrConfiguration;
import fr.rakambda.plexdeleter.config.SeerrConfiguration;
import fr.rakambda.plexdeleter.config.SonarrConfiguration;
import fr.rakambda.plexdeleter.storage.entity.MediaEntity;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class ThymeleafService{
	private final String seerrEndpoint;
	private final String radarrEndpoint;
	private final String sonarrEndpoint;
	private final String plexEndpoint;
	private final String plexServerId;
	private final String applicationEndpoint;
	
	@Autowired
	public ThymeleafService(ApplicationConfiguration applicationConfiguration, SeerrConfiguration seerrConfiguration, PlexConfiguration plexConfiguration, RadarrConfiguration radarrConfiguration, SonarrConfiguration sonarrConfiguration){
		this.seerrEndpoint = seerrConfiguration.endpoint();
		this.radarrEndpoint = radarrConfiguration.endpoint();
		this.sonarrEndpoint = sonarrConfiguration.endpoint();
		this.plexEndpoint = plexConfiguration.appEndpoint();
		this.plexServerId = plexConfiguration.serverId();
		this.applicationEndpoint = applicationConfiguration.getServer().getApplicationUrl();
	}
	
	@Nullable
	public String getMediaSeerrUrl(@NonNull MediaEntity media){
		return Optional.ofNullable(media.getSeerrId())
				.map(id -> "%s/%s/%d".formatted(seerrEndpoint, media.getType().getSeerrType().getValue(), id))
				.orElse(null);
	}
	
	@Nullable
	public String getMediaSonarrUrl(@NonNull MediaEntity media){
		return Optional.ofNullable(media.getSonarrSlug())
				.map(id -> "%s/series/%s".formatted(sonarrEndpoint, id))
				.orElse(null);
	}
	
	@Nullable
	public String getMediaRadarrUrl(@NonNull MediaEntity media){
		return Optional.ofNullable(media.getRadarrSlug())
				.map(id -> "%s/movie/%s".formatted(radarrEndpoint, id))
				.orElse(null);
	}
	
	@Nullable
	public String getMediaPlexUrl(@NonNull MediaEntity media){
		return Optional.ofNullable(media.getPlexId())
				.map(this::getRatingKeyPlexUrl)
				.orElse(null);
	}
	
	@Nullable
	public String getMediaTraktUrl(@NonNull MediaEntity media){
		return Optional.ofNullable(media.getTmdbId())
				.map(id -> "https://trakt.tv/search/tmdb/%d?id_type=%s".formatted(id, media.getType().getSeerrType().getTraktSearchValue()))
				.orElse(null);
	}
	
	@Nullable
	public String getRatingKeyPlexUrl(int ratingKey){
		return "%s/desktop/#!/server/%s/details?key=%%2Flibrary%%2Fmetadata%%2F%d".formatted(plexEndpoint, plexServerId, ratingKey);
	}
	
	@Nullable
	public String getMediaTmdbUrl(@NonNull MediaEntity media){
		var type = switch(media.getType()){
			case MOVIE -> "movie";
			case SEASON -> "tv";
		};
		
		return Optional.ofNullable(media.getTmdbId())
				.map(id -> "https://www.themoviedb.org/%s/%d".formatted(type, id))
				.orElse(null);
	}
	
	@Nullable
	public String getMediaTvdbUrl(@NonNull MediaEntity media){
		var type = switch(media.getType()){
			case MOVIE -> "movie";
			case SEASON -> "series";
		};
		
		return Optional.ofNullable(media.getTvdbId())
				.map(slug -> "https://www.thetvdb.com/dereferrer/%s/%s".formatted(type, slug))
				.orElse(null);
	}
	
	@Nullable
	public String getOwnUrl(){
		return applicationEndpoint;
	}
	
	@NonNull
	public String getAddWatchMediaUrl(int mediaId){
		return "%s/api/user/media-requirement/add?media=%d".formatted(getOwnUrl(), mediaId);
	}
	
	@Nullable
	public String getTableColorClass(@NonNull MediaEntity media){
		return media.getStatus().getTableClass();
	}
}
