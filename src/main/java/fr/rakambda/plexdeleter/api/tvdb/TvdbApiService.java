package fr.rakambda.plexdeleter.api.tvdb;

import fr.rakambda.plexdeleter.api.ClientLoggerRequestInterceptor;
import fr.rakambda.plexdeleter.api.HttpUtils;
import fr.rakambda.plexdeleter.api.RequestFailedException;
import fr.rakambda.plexdeleter.api.tvdb.data.LoginRequest;
import fr.rakambda.plexdeleter.api.tvdb.data.LoginResponse;
import fr.rakambda.plexdeleter.api.tvdb.data.MovieData;
import fr.rakambda.plexdeleter.api.tvdb.data.SeriesData;
import fr.rakambda.plexdeleter.api.tvdb.data.Translation;
import fr.rakambda.plexdeleter.api.tvdb.data.TvdbResponseWrapper;
import fr.rakambda.plexdeleter.config.TvdbConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Slf4j
@Service
public class TvdbApiService{
	private final RestClient apiClient;
	
	private String bearerToken;
	
	public TvdbApiService(TvdbConfiguration tvdbConfiguration, ClientLoggerRequestInterceptor clientLoggerRequestInterceptor){
		apiClient = RestClient.builder()
				.baseUrl(tvdbConfiguration.endpoint())
				.requestInterceptor((request, body, execution) -> {
					if(Objects.equals(request.getURI().getPath(), "/v4/login")){
						return execution.execute(request, body);
					}
					var bearer = getBearer(tvdbConfiguration.apiKey());
					request.getHeaders().add(AUTHORIZATION, "Bearer " + bearer);
					return execution.execute(request, body);
				})
				.requestInterceptor(clientLoggerRequestInterceptor)
				.build();
	}
	
	@Nullable
	private String getBearer(@NonNull String apiKey){
		if(Objects.nonNull(bearerToken)){
			return bearerToken;
		}
		
		try{
			var response = login(apiKey);
			bearerToken = Optional.ofNullable(response.getData())
					.map(LoginResponse::getToken)
					.orElse(null);
		}
		catch(Exception e){
			log.error("Failed to login on Tvdb");
		}
		return bearerToken;
	}
	
	@NonNull
	private TvdbResponseWrapper<LoginResponse> login(@NonNull String apiKey) throws RequestFailedException{
		log.info("Logging in on Tvdb");
		var data = LoginRequest.builder()
				.apikey(apiKey)
				.build();
		
		return HttpUtils.unwrapIfStatusOkAndNotNullBody(apiClient.post()
				.uri("/v4/login")
				.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
				.body(data)
				.retrieve()
				.toEntity(new ParameterizedTypeReference<>(){}));
	}
	
	@NonNull
	public TvdbResponseWrapper<MovieData> getExtendedMovieData(int movieId) throws RequestFailedException{
		log.info("Getting extended movie data from Tvdb");
		return HttpUtils.unwrapIfStatusOkAndNotNullBody(apiClient.get()
				.uri("/v4/movies/{movieId}/extended", movieId)
				.retrieve()
				.toEntity(new ParameterizedTypeReference<>(){}));
	}
	
	@NonNull
	public TvdbResponseWrapper<SeriesData> getExtendedSeriesData(int seriesId) throws RequestFailedException{
		log.info("Getting extended series data from Tvdb");
		return HttpUtils.unwrapIfStatusOkAndNotNullBody(apiClient.get()
				.uri("/v4/series/{seriesId}/extended", seriesId)
				.retrieve()
				.toEntity(new ParameterizedTypeReference<>(){}));
	}
	
	@NonNull
	public TvdbResponseWrapper<Translation> getMovieTranslations(int movieId, @NonNull Locale locale) throws RequestFailedException{
		log.info("Getting movie translations from Tvdb");
		return HttpUtils.unwrapIfStatusOkAndNotNullBody(apiClient.get()
				.uri("/v4/movies/{movieId}/translations/{lang}", movieId, locale.getISO3Language())
				.retrieve()
				.toEntity(new ParameterizedTypeReference<>(){}));
	}
	
	@NonNull
	public TvdbResponseWrapper<Translation> getSeriesTranslations(int seriesId, @NonNull Locale locale) throws RequestFailedException{
		log.info("Getting series translations from Tvdb");
		return HttpUtils.unwrapIfStatusOkAndNotNullBody(apiClient.get()
				.uri("/v4/series/{seriesId}/translations/{lang}", seriesId, locale.getISO3Language())
				.retrieve()
				.toEntity(new ParameterizedTypeReference<>(){}));
	}
	
	@NonNull
	public TvdbResponseWrapper<SeriesData> getEpisodes(int seriesId) throws RequestFailedException{
		log.info("Getting episodes from Tvdb");
		return HttpUtils.unwrapIfStatusOkAndNotNullBody(apiClient.get()
				.uri("/v4/series/{seriesId}/episodes/{type}", seriesId, "default")
				.retrieve()
				.toEntity(new ParameterizedTypeReference<>(){}));
	}
	
	@NonNull
	public TvdbResponseWrapper<Translation> getEpisodeTranslations(int episodeId, @NonNull Locale locale) throws RequestFailedException{
		log.info("Getting episode translations from Tvdb");
		return HttpUtils.unwrapIfStatusOkAndNotNullBody(apiClient.get()
				.uri("/v4/episodes/{episodeId}/translations/{lang}", episodeId, locale.getISO3Language())
				.retrieve()
				.toEntity(new ParameterizedTypeReference<>(){}));
	}
	
	@NonNull
	public TvdbResponseWrapper<Translation> getSeasonTranslations(int episodeId, @NonNull Locale locale) throws RequestFailedException{
		log.info("Getting season translations from Tvdb");
		return HttpUtils.unwrapIfStatusOkAndNotNullBody(apiClient.get()
				.uri("/v4/seasons/{episodeId}/translations/{lang}", episodeId, locale.getISO3Language())
				.retrieve()
				.toEntity(new ParameterizedTypeReference<>(){}));
	}
}
