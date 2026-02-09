package fr.rakambda.plexdeleter.api.tautulli;

import fr.rakambda.plexdeleter.api.ClientLoggerRequestInterceptor;
import fr.rakambda.plexdeleter.api.HttpUtils;
import fr.rakambda.plexdeleter.api.RequestFailedException;
import fr.rakambda.plexdeleter.api.RetryInterceptor;
import fr.rakambda.plexdeleter.api.tautulli.data.GetHistoryResponse;
import fr.rakambda.plexdeleter.api.tautulli.data.GetLibraryMediaInfo;
import fr.rakambda.plexdeleter.api.tautulli.data.GetMetadataResponse;
import fr.rakambda.plexdeleter.api.tautulli.data.GetNewRatingKeysData;
import fr.rakambda.plexdeleter.api.tautulli.data.GetNewRatingKeysResponse;
import fr.rakambda.plexdeleter.api.tautulli.data.TautulliResponseWrapper;
import fr.rakambda.plexdeleter.config.TautulliConfiguration;
import fr.rakambda.plexdeleter.storage.entity.MediaType;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.support.HttpRequestWrapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import java.net.URI;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import static java.time.temporal.ChronoUnit.MILLIS;
import static org.springframework.http.HttpStatus.BAD_GATEWAY;

@Slf4j
@Service
public class TautulliApiService{
	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	
	private final RestClient apiClient;
	
	public TautulliApiService(TautulliConfiguration tautulliConfiguration, ClientLoggerRequestInterceptor clientLoggerRequestInterceptor){
		apiClient = RestClient.builder()
				.baseUrl(tautulliConfiguration.endpoint())
				.requestInterceptor((request, body, execution) -> {
					var uri = UriComponentsBuilder.fromUri(request.getURI())
							.queryParam("apikey", tautulliConfiguration.apiKey())
							.build().toUri();
					
					var modifiedRequest = new HttpRequestWrapper(request){
						@Override
						@NonNull
						public URI getURI(){
							return uri;
						}
					};
					
					return execution.execute(modifiedRequest, body);
				})
				.requestInterceptor(new RetryInterceptor(100, 60_000, MILLIS, BAD_GATEWAY))
				.requestInterceptor(clientLoggerRequestInterceptor)
				.build();
	}
	
	@NonNull
	public Collection<Integer> getElementsRatingKeys(int ratingKey, @NonNull MediaType mediaType) throws RequestFailedException{
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
					.filter(Objects::nonNull)
					.map(Map::values)
					.flatMap(Collection::stream)
					.map(GetNewRatingKeysData::getRatingKey)
					.distinct()
					.toList();
		};
	}
	
	@NonNull
	public Optional<Integer> getSeasonRatingKey(int ratingKey, int season) throws RequestFailedException{
		return Optional.ofNullable(getNewRatingKeys(ratingKey, "show").getResponse().getData())
				.map(GetNewRatingKeysResponse::getData)
				.map(GetNewRatingKeysData::getChildren)
				.map(m -> m.get(String.valueOf(season)))
				.map(GetNewRatingKeysData::getRatingKey);
	}
	
	@NonNull
	private TautulliResponseWrapper<GetNewRatingKeysResponse> getNewRatingKeys(int ratingKey, @NonNull String mediaType) throws RequestFailedException{
		log.info("Getting new rating keys for Plex id {} and type {}", ratingKey, mediaType);
		return HttpUtils.unwrapIfStatusOkAndNotNullBody(apiClient.get()
				.uri(b -> b.pathSegment("api", "v2")
						.queryParam("cmd", "get_new_rating_keys")
						.queryParam("rating_key", ratingKey)
						.queryParam("media_type", mediaType)
						.build())
				.retrieve()
				.toEntity(new ParameterizedTypeReference<>(){}));
	}
	
	@NonNull
	public TautulliResponseWrapper<GetMetadataResponse> getMetadata(int ratingKey) throws RequestFailedException{
		log.info("Getting metadata for Plex id {}", ratingKey);
		return HttpUtils.unwrapIfStatusOkAndNotNullBody(apiClient.get()
				.uri(b -> b.pathSegment("api", "v2")
						.queryParam("cmd", "get_metadata")
						.queryParam("rating_key", ratingKey)
						.build())
				.retrieve()
				.toEntity(new ParameterizedTypeReference<>(){}));
	}
	
	@NonNull
	public TautulliResponseWrapper<GetHistoryResponse> getHistory(int ratingKey, @NonNull MediaType mediaType, int userId, @Nullable Instant after) throws RequestFailedException{
		return switch(mediaType){
			case MOVIE -> getHistory(ratingKey, "rating_key", userId, "movie", after);
			case SEASON -> getHistory(ratingKey, "parent_rating_key", userId, "episode", after);
		};
	}
	
	@NonNull
	private TautulliResponseWrapper<GetHistoryResponse> getHistory(int ratingKey, @NonNull String ratingKeyParamName, int userId, @NonNull String mediaType, @Nullable Instant after) throws RequestFailedException{
		log.info("Getting history for Plex id {} of type {} and user id {}", ratingKey, mediaType, userId);
		return HttpUtils.unwrapIfStatusOkAndNotNullBody(apiClient.get()
				.uri(b -> {
					b = b.pathSegment("api", "v2")
							.queryParam("cmd", "get_history")
							.queryParam(ratingKeyParamName, ratingKey)
							.queryParam("user_id", userId)
							.queryParam("media_type", mediaType)
							.queryParam("length", 10000)
							.queryParam("grouping", 1);
					if(Objects.nonNull(after)){
						b = b.queryParam("after", DATE_FORMAT.format(ZonedDateTime.ofInstant(after, ZoneId.systemDefault())));
					}
					return b.build();
				})
				.retrieve()
				.toEntity(new ParameterizedTypeReference<>(){}));
	}
	
	@NonNull
	public TautulliResponseWrapper<GetLibraryMediaInfo> getLibraryMediaInfo(int sectionId) throws RequestFailedException{
		log.info("Getting library media info for Section id {}", sectionId);
		return HttpUtils.unwrapIfStatusOkAndNotNullBody(apiClient.get()
				.uri(b -> b.pathSegment("api", "v2")
						.queryParam("cmd", "get_library_media_info")
						.queryParam("section_id", sectionId)
						.queryParam("refresh", true)
						.queryParam("length", 10000)
						.build())
				.retrieve()
				.toEntity(new ParameterizedTypeReference<>(){}));
	}
	
	@NonNull
	public Optional<byte[]> getPosterBytes(int ratingKey, int width, int height){
		log.info("Getting poster bytes for Plex id {}", ratingKey);
		return Optional.ofNullable(apiClient.get()
				.uri(b -> b.pathSegment("pms_image_proxy")
						.queryParam("width", width)
						.queryParam("height", height)
						.queryParam("rating_key", ratingKey)
						.build())
				.accept(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM)
				.retrieve()
				.body(byte[].class));
	}
}
