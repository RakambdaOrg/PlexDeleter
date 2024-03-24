package fr.rakambda.plexdeleter.api.tmdb;

import fr.rakambda.plexdeleter.api.HttpUtils;
import fr.rakambda.plexdeleter.api.RequestFailedException;
import fr.rakambda.plexdeleter.api.tmdb.data.MovieData;
import fr.rakambda.plexdeleter.api.tmdb.data.SeasonData;
import fr.rakambda.plexdeleter.api.tmdb.data.SeriesData;
import fr.rakambda.plexdeleter.config.ApplicationConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.Locale;

@Slf4j
@Service
public class TmdbService{
	private final WebClient apiClient;
	
	public TmdbService(ApplicationConfiguration applicationConfiguration){
		var tmdbConfiguration = applicationConfiguration.getTmdb();
		
		apiClient = WebClient.builder()
				.baseUrl(tmdbConfiguration.getEndpoint())
				.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + tmdbConfiguration.getToken())
				.build();
	}
	
	@NotNull
	public MovieData getMovieData(int movieId, @NotNull Locale locale) throws RequestFailedException{
		log.info("Getting movie data from Tmdb");
		return HttpUtils.unwrapIfStatusOkAndNotNullBody(apiClient.get()
				.uri(b -> b.pathSegment("3", "movie", "{movieId}")
						.queryParam("language", locale.getLanguage())
						.build(movieId))
				.retrieve()
				.toEntity(new ParameterizedTypeReference<MovieData>(){})
				.blockOptional()
				.orElseThrow(() -> new RequestFailedException("Failed to get movie data from Tmdb with id %d".formatted(movieId))));
	}
	
	@NotNull
	public SeriesData getSeriesData(int seriesId, @NotNull Locale locale) throws RequestFailedException{
		log.info("Getting series data from Tmdb");
		return HttpUtils.unwrapIfStatusOkAndNotNullBody(apiClient.get()
				.uri(b -> b.pathSegment("3", "tv", "{seriesId}")
						.queryParam("language", locale.getLanguage())
						.build(seriesId))
				.retrieve()
				.toEntity(new ParameterizedTypeReference<SeriesData>(){})
				.blockOptional()
				.orElseThrow(() -> new RequestFailedException("Failed to get series data from Tmdb with id %d".formatted(seriesId))));
	}
	
	@NotNull
	public SeasonData getSeasonData(int seriesId, int season, @NotNull Locale locale) throws RequestFailedException{
		log.info("Getting season data from Tmdb");
		return HttpUtils.unwrapIfStatusOkAndNotNullBody(apiClient.get()
				.uri(b -> b.pathSegment("3", "tv", "{seriesId}", "season", "{season}")
						.queryParam("language", locale.getLanguage())
						.build(seriesId, season))
				.retrieve()
				.toEntity(new ParameterizedTypeReference<SeasonData>(){})
				.blockOptional()
				.orElseThrow(() -> new RequestFailedException("Failed to get season data from Tmdb with id %d and index %d".formatted(seriesId, season))));
	}
}
