package fr.rakambda.plexdeleter.api.tmdb;

import fr.rakambda.plexdeleter.api.ClientLoggerRequestInterceptor;
import fr.rakambda.plexdeleter.api.HttpUtils;
import fr.rakambda.plexdeleter.api.RequestFailedException;
import fr.rakambda.plexdeleter.api.tmdb.data.MovieData;
import fr.rakambda.plexdeleter.api.tmdb.data.SeasonData;
import fr.rakambda.plexdeleter.api.tmdb.data.SeriesData;
import fr.rakambda.plexdeleter.config.TmdbConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import java.util.Locale;

@Slf4j
@Service
public class TmdbApiService{
	private final RestClient apiClient;
	
	public TmdbApiService(TmdbConfiguration tmdbConfiguration, ClientLoggerRequestInterceptor clientLoggerRequestInterceptor){
		apiClient = RestClient.builder()
				.baseUrl(tmdbConfiguration.endpoint())
				.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + tmdbConfiguration.token())
				.requestInterceptor(clientLoggerRequestInterceptor)
				.build();
	}
	
	@NonNull
	public MovieData getMovieData(int movieId, @NonNull Locale locale) throws RequestFailedException{
		log.info("Getting movie data from Tmdb");
		return HttpUtils.unwrapIfStatusOkAndNotNullBody(apiClient.get()
				.uri("/3/movie/{movieId}", b -> b
						.queryParam("language", locale.getLanguage())
						.build(movieId))
				.retrieve()
				.toEntity(new ParameterizedTypeReference<>(){}));
	}
	
	@NonNull
	public SeriesData getSeriesData(int seriesId, @NonNull Locale locale) throws RequestFailedException{
		log.info("Getting series data from Tmdb");
		return HttpUtils.unwrapIfStatusOkAndNotNullBody(apiClient.get()
				.uri("/3/tv/{seriesId}", b -> b
						.queryParam("language", locale.getLanguage())
						.build(seriesId))
				.retrieve()
				.toEntity(new ParameterizedTypeReference<>(){}));
	}
	
	@NonNull
	public SeasonData getSeasonData(int seriesId, int season, @NonNull Locale locale) throws RequestFailedException{
		log.info("Getting season data from Tmdb");
		return HttpUtils.unwrapIfStatusOkAndNotNullBody(apiClient.get()
				.uri("/3/tv/{seriesId}/season/{season}", b -> b
						.queryParam("language", locale.getLanguage())
						.build(seriesId, season))
				.retrieve()
				.toEntity(new ParameterizedTypeReference<>(){}));
	}
}
