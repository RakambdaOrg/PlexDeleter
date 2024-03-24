package fr.rakambda.plexdeleter.api.tvdb;

import fr.rakambda.plexdeleter.api.HttpUtils;
import fr.rakambda.plexdeleter.api.RequestFailedException;
import fr.rakambda.plexdeleter.api.tvdb.data.LoginRequest;
import fr.rakambda.plexdeleter.api.tvdb.data.LoginResponse;
import fr.rakambda.plexdeleter.api.tvdb.data.MovieData;
import fr.rakambda.plexdeleter.api.tvdb.data.SeriesData;
import fr.rakambda.plexdeleter.api.tvdb.data.Translation;
import fr.rakambda.plexdeleter.api.tvdb.data.TvdbResponseWrapper;
import fr.rakambda.plexdeleter.config.ApplicationConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
public class TvdbService{
	private final WebClient apiClient;
	
	private String bearerToken;
	
	public TvdbService(ApplicationConfiguration applicationConfiguration){
		var tvdbConfiguration = applicationConfiguration.getTvdb();
		
		apiClient = WebClient.builder()
				.baseUrl(tvdbConfiguration.getEndpoint())
				.filter(ExchangeFilterFunction.ofRequestProcessor(req -> {
					if(Objects.equals(req.url().getPath(), "/v4/login")){
						return Mono.just(req);
					}
					return Mono.just(ClientRequest.from(req)
							.header(HttpHeaders.AUTHORIZATION, "Bearer " + getBearer(tvdbConfiguration.getApiKey()))
							.build());
				}))
				.build();
	}
	
	@Nullable
	private String getBearer(@NotNull String apiKey){
		if(Objects.nonNull(bearerToken)){
			return bearerToken;
		}
		
		try{
			bearerToken = Optional.ofNullable(login(apiKey).getData())
					.map(LoginResponse::getToken)
					.orElse(null);
		}
		catch(Exception e){
			log.error("Failed to login on Tvdb");
		}
		return bearerToken;
	}
	
	@NotNull
	private TvdbResponseWrapper<LoginResponse> login(@NotNull String apiKey) throws RequestFailedException{
		log.info("Logging in on Tvdb");
		var data = LoginRequest.builder()
				.apikey(apiKey)
				.build();
		
		return HttpUtils.unwrapIfStatusOkAndNotNullBody(apiClient.post()
				.uri(b -> b.pathSegment("v4", "login")
						.build())
				.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
				.body(BodyInserters.fromValue(data))
				.retrieve()
				.toEntity(new ParameterizedTypeReference<TvdbResponseWrapper<LoginResponse>>(){})
				.blockOptional()
				.orElseThrow(() -> new RequestFailedException("Failed to log in on Tvdb")));
	}
	
	@NotNull
	public TvdbResponseWrapper<MovieData> getExtendedMovieData(int movieId) throws RequestFailedException{
		log.info("Getting extended movie data from Tvdb");
		return HttpUtils.unwrapIfStatusOkAndNotNullBody(apiClient.get()
				.uri(b -> b.pathSegment("v4", "movies", "{movieId}", "extended")
						.build(movieId))
				.retrieve()
				.toEntity(new ParameterizedTypeReference<TvdbResponseWrapper<MovieData>>(){})
				.blockOptional()
				.orElseThrow(() -> new RequestFailedException("Failed to get extended movie data from Tvdb with id %d".formatted(movieId))));
	}
	
	@NotNull
	public TvdbResponseWrapper<SeriesData> getExtendedSeriesData(int seriesId) throws RequestFailedException{
		log.info("Getting extended series data from Tvdb");
		return HttpUtils.unwrapIfStatusOkAndNotNullBody(apiClient.get()
				.uri(b -> b.pathSegment("v4", "series", "{seriesId}", "extended")
						.build(seriesId))
				.retrieve()
				.toEntity(new ParameterizedTypeReference<TvdbResponseWrapper<SeriesData>>(){})
				.blockOptional()
				.orElseThrow(() -> new RequestFailedException("Failed to get extended series data from Tvdb with id %d".formatted(seriesId))));
	}
	
	@NotNull
	public TvdbResponseWrapper<Translation> getMovieTranslations(int movieId, @NotNull Locale locale) throws RequestFailedException{
		log.info("Getting movie translations from Tvdb");
		return HttpUtils.unwrapIfStatusOkAndNotNullBody(apiClient.get()
				.uri(b -> b.pathSegment("v4", "movies", "{movieId}", "translations", "{lang}")
						.build(movieId, locale.getISO3Language()))
				.retrieve()
				.toEntity(new ParameterizedTypeReference<TvdbResponseWrapper<Translation>>(){})
				.blockOptional()
				.orElseThrow(() -> new RequestFailedException("Failed to get movie translations from Tvdb with id %d and locale %s".formatted(movieId, locale))));
	}
	
	@NotNull
	public TvdbResponseWrapper<Translation> getSeriesTranslations(int seriesId, @NotNull Locale locale) throws RequestFailedException{
		log.info("Getting series translations from Tvdb");
		return HttpUtils.unwrapIfStatusOkAndNotNullBody(apiClient.get()
				.uri(b -> b.pathSegment("v4", "series", "{seriesId}", "translations", "{lang}")
						.build(seriesId, locale.getISO3Language()))
				.retrieve()
				.toEntity(new ParameterizedTypeReference<TvdbResponseWrapper<Translation>>(){})
				.blockOptional()
				.orElseThrow(() -> new RequestFailedException("Failed to get series translations from Tvdb with id %d and locale %s".formatted(seriesId, locale))));
	}
	
	@NotNull
	public TvdbResponseWrapper<SeriesData> getEpisodes(int seriesId) throws RequestFailedException{
		log.info("Getting episodes from Tvdb");
		return HttpUtils.unwrapIfStatusOkAndNotNullBody(apiClient.get()
				.uri(b -> b.pathSegment("v4", "series", "{seriesId}", "episodes", "{type}")
						.build(seriesId, "default"))
				.retrieve()
				.toEntity(new ParameterizedTypeReference<TvdbResponseWrapper<SeriesData>>(){})
				.blockOptional()
				.orElseThrow(() -> new RequestFailedException("Failed to get episodes from Tvdb for series with id %d".formatted(seriesId))));
	}
	
	@NotNull
	public TvdbResponseWrapper<Translation> getEpisodeTranslations(int episodeId, @NotNull Locale locale) throws RequestFailedException{
		log.info("Getting episode translations from Tvdb");
		return HttpUtils.unwrapIfStatusOkAndNotNullBody(apiClient.get()
				.uri(b -> b.pathSegment("v4", "episodes", "{episodeId}", "translations", "{lang}")
						.build(episodeId, locale.getISO3Language()))
				.retrieve()
				.toEntity(new ParameterizedTypeReference<TvdbResponseWrapper<Translation>>(){})
				.blockOptional()
				.orElseThrow(() -> new RequestFailedException("Failed to get episode translations from Tvdb with id %d and locale %s".formatted(episodeId, locale))));
	}
	
	@NotNull
	public TvdbResponseWrapper<Translation> getSeasonTranslations(int episodeId, @NotNull Locale locale) throws RequestFailedException{
		log.info("Getting season translations from Tvdb");
		return HttpUtils.unwrapIfStatusOkAndNotNullBody(apiClient.get()
				.uri(b -> b.pathSegment("v4", "seasons", "{episodeId}", "translations", "{lang}")
						.build(episodeId, locale.getISO3Language()))
				.retrieve()
				.toEntity(new ParameterizedTypeReference<TvdbResponseWrapper<Translation>>(){})
				.blockOptional()
				.orElseThrow(() -> new RequestFailedException("Failed to get season translations from Tvdb with id %d and locale %s".formatted(episodeId, locale))));
	}
}
