package fr.rakambda.plexdeleter.api.tautulli;

import fr.rakambda.plexdeleter.api.HttpUtils;
import fr.rakambda.plexdeleter.api.RequestFailedException;
import fr.rakambda.plexdeleter.api.tautulli.data.GetHistoryResponse;
import fr.rakambda.plexdeleter.api.tautulli.data.GetMetadataResponse;
import fr.rakambda.plexdeleter.api.tautulli.data.GetNewRatingKeysData;
import fr.rakambda.plexdeleter.api.tautulli.data.GetNewRatingKeysResponse;
import fr.rakambda.plexdeleter.api.tautulli.data.TautulliResponseWrapper;
import fr.rakambda.plexdeleter.config.ApplicationConfiguration;
import fr.rakambda.plexdeleter.storage.entity.MediaType;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
public class TautulliService{
	private final WebClient apiClient;
	
	public TautulliService(ApplicationConfiguration applicationConfiguration){
		apiClient = WebClient.builder()
				.baseUrl(applicationConfiguration.getTautulli().getEndpoint())
				.filter(ExchangeFilterFunction.ofRequestProcessor(req -> Mono.just(ClientRequest.from(req)
						.url(UriComponentsBuilder.fromUri(req.url())
								.queryParam("apikey", applicationConfiguration.getTautulli().getApiKey())
								.build(true)
								.toUri())
						.build())
				))
				// .filter(HttpUtils.logErrorFilter())
				.build();
	}
	
	@NotNull
	public Collection<Integer> getElementsRatingKeys(int ratingKey, @NotNull MediaType mediaType) throws RequestFailedException{
		return switch(mediaType){
			case MOVIE -> getNewRatingKeys(ratingKey, "movie").getResponse().getDataOptional()
					.map(GetNewRatingKeysResponse::getData)
					.map(GetNewRatingKeysData::getRatingKey)
					.map(Set::of)
					.orElseGet(Set::of);
			case SEASON -> getNewRatingKeys(ratingKey, "season").getResponse().getDataOptional()
					.map(GetNewRatingKeysResponse::getData)
					.map(GetNewRatingKeysData::getChildren)
					.orElseGet(Map::of)
					.values().stream()
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
	public Optional<Integer> getSeasonRatingKey(int ratingKey, int season) throws RequestFailedException{
		return Optional.ofNullable(getNewRatingKeys(ratingKey, "show").getResponse().getData())
				.map(GetNewRatingKeysResponse::getData)
				.map(GetNewRatingKeysData::getChildren)
				.map(m -> m.get(String.valueOf(season)))
				.map(GetNewRatingKeysData::getRatingKey);
	}
	
	@NotNull
	private TautulliResponseWrapper<GetNewRatingKeysResponse> getNewRatingKeys(int ratingKey, @NotNull String mediaType) throws RequestFailedException{
		log.info("Getting new rating keys for Plex id {} and type {}", ratingKey, mediaType);
		return HttpUtils.unwrapIfStatusOkAndNotNullBody(apiClient.get()
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
		log.info("Getting metadata for Plex id {}", ratingKey);
		return HttpUtils.unwrapIfStatusOkAndNotNullBody(apiClient.get()
				.uri(b -> b.pathSegment("api", "v2")
						.queryParam("cmd", "get_metadata")
						.queryParam("rating_key", ratingKey)
						.build())
				.retrieve()
				.toEntity(new ParameterizedTypeReference<TautulliResponseWrapper<GetMetadataResponse>>(){})
				.blockOptional()
				.orElseThrow(() -> new RequestFailedException("Failed to get metadata with rating key %d".formatted(ratingKey))));
	}
	
	@NotNull
	public TautulliResponseWrapper<GetHistoryResponse> getHistory(int ratingKey, @NotNull MediaType mediaType, int userId) throws RequestFailedException{
		return switch(mediaType){
			case MOVIE -> getHistory(ratingKey, "rating_key", userId, "movie");
			case SEASON -> getHistory(ratingKey, "parent_rating_key", userId, "episode");
		};
	}
	
	@NotNull
	private TautulliResponseWrapper<GetHistoryResponse> getHistory(int ratingKey, @NotNull String ratingKeyParamName, int userId, @NotNull String mediaType) throws RequestFailedException{
		log.info("Getting history for Plex id {} of type {} and user id {}", ratingKey, mediaType, userId);
		return HttpUtils.unwrapIfStatusOkAndNotNullBody(apiClient.get()
				.uri(b -> b.pathSegment("api", "v2")
						.queryParam("cmd", "get_history")
						.queryParam(ratingKeyParamName, ratingKey)
						.queryParam("user_id", userId)
						.queryParam("media_type", mediaType)
						.queryParam("length", 10000)
						.queryParam("grouping ", 1)
						.build())
				.retrieve()
				.toEntity(new ParameterizedTypeReference<TautulliResponseWrapper<GetHistoryResponse>>(){})
				.blockOptional()
				.orElseThrow(() -> new RequestFailedException("Failed to get metadata with rating key %d".formatted(ratingKey))));
	}
	
	@NotNull
	public Optional<byte[]> getPosterBytes(int ratingKey, int width, int height){
		log.info("Getting poster bytes for Plex id {}", ratingKey);
		return apiClient.get()
				.uri(b -> b.pathSegment("pms_image_proxy")
						.queryParam("width", width)
						.queryParam("height", height)
						.queryParam("rating_key", ratingKey)
						.build())
				.accept(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM)
				.retrieve()
				.toEntity(byte[].class)
				.blockOptional()
				.map(ResponseEntity::getBody);
	}
}
