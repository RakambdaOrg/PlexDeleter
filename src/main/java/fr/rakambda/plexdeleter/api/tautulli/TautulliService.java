package fr.rakambda.plexdeleter.api.tautulli;

import fr.rakambda.plexdeleter.api.HttpUtils;
import fr.rakambda.plexdeleter.api.RequestFailedException;
import fr.rakambda.plexdeleter.api.tautulli.data.GetMetadataResponse;
import fr.rakambda.plexdeleter.api.tautulli.data.GetNewRatingKeysData;
import fr.rakambda.plexdeleter.api.tautulli.data.GetNewRatingKeysResponse;
import fr.rakambda.plexdeleter.api.tautulli.data.TautulliResponseWrapper;
import fr.rakambda.plexdeleter.config.ApplicationConfiguration;
import fr.rakambda.plexdeleter.storage.entity.MediaType;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Slf4j
@Service
public class TautulliService{
	private final WebClient apiClient;
	
	public TautulliService(ApplicationConfiguration applicationConfiguration){
		apiClient = WebClient.builder()
				.baseUrl(applicationConfiguration.getTautulli().getEndpoint())
				.filter(ExchangeFilterFunction.ofRequestProcessor(req ->
						Mono.just(ClientRequest.from(req)
								.url(UriComponentsBuilder.fromUri(req.url())
										.queryParam("apikey", applicationConfiguration.getTautulli().getApiKey())
										.build(true)
										.toUri())
								.build())
				))
				.filter(logRequest())
				.build();
	}
	
	private static ExchangeFilterFunction logRequest(){
		return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
			log.info("Request: {} {}", clientRequest.method(), clientRequest.url());
			clientRequest.headers().forEach((name, values) -> values.forEach(value -> log.info("{}={}", name, value)));
			return Mono.just(clientRequest);
		});
	}
	
	@NotNull
	public Collection<Integer> getElementsRatingKeys(int ratingKey, @NotNull MediaType mediaType) throws RequestFailedException{
		return switch(mediaType){
			case MOVIE -> Set.of(getNewRatingKeys(ratingKey, "movie").getResponse().getData().getData().getRatingKey());
			case SEASON -> getNewRatingKeys(ratingKey, "season").getResponse().getData().getData()
					.getChildren().values().stream()
					.filter(data -> Objects.equals(data.getRatingKey(), ratingKey))
					.map(GetNewRatingKeysData::getChildren)
					.map(Map::values)
					.flatMap(Collection::stream)
					.map(GetNewRatingKeysData::getRatingKey)
					.distinct()
					.toList();
		};
	}
	
	@NotNull
	private TautulliResponseWrapper<GetNewRatingKeysResponse> getNewRatingKeys(int ratingKey, @NotNull String mediaType) throws RequestFailedException{
		return HttpUtils.withStatusOkAndBody(apiClient.get()
				.uri(b -> b.pathSegment("api", "v2")
						.queryParam("cmd", "get_new_rating_keys")
						.queryParam("rating_key", ratingKey)
						.queryParam("media_type", mediaType)
						.build())
				.retrieve()
				.toEntity(new ParameterizedTypeReference<TautulliResponseWrapper<GetNewRatingKeysResponse>>(){})
				.blockOptional()
				.orElseThrow(() -> new RequestFailedException("Failed to new rating key %s with type %s".formatted(ratingKey, mediaType))));
	}
	
	@NotNull
	public TautulliResponseWrapper<GetMetadataResponse> getMetadata(int ratingKey) throws RequestFailedException{
		return HttpUtils.withStatusOkAndBody(apiClient.get()
				.uri(b -> b.pathSegment("api", "v2")
						.queryParam("cmd", "get_metadata")
						.queryParam("rating_key", ratingKey)
						.build())
				.retrieve()
				.toEntity(new ParameterizedTypeReference<TautulliResponseWrapper<GetMetadataResponse>>(){})
				.blockOptional()
				.orElseThrow(() -> new RequestFailedException("Failed to get metadata with rating key %d".formatted(ratingKey))));
	}
}
