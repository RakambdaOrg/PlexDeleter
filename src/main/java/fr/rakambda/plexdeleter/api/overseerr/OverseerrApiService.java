package fr.rakambda.plexdeleter.api.overseerr;

import fr.rakambda.plexdeleter.api.HttpUtils;
import fr.rakambda.plexdeleter.api.RequestFailedException;
import fr.rakambda.plexdeleter.api.RetryInterceptor;
import fr.rakambda.plexdeleter.api.overseerr.data.Media;
import fr.rakambda.plexdeleter.api.overseerr.data.MediaType;
import fr.rakambda.plexdeleter.api.overseerr.data.MovieMedia;
import fr.rakambda.plexdeleter.api.overseerr.data.PagedResponse;
import fr.rakambda.plexdeleter.api.overseerr.data.PlexSyncRequest;
import fr.rakambda.plexdeleter.api.overseerr.data.PlexSyncResponse;
import fr.rakambda.plexdeleter.api.overseerr.data.Request;
import fr.rakambda.plexdeleter.api.overseerr.data.RequestSeason;
import fr.rakambda.plexdeleter.api.overseerr.data.SeriesMedia;
import fr.rakambda.plexdeleter.config.ApplicationConfiguration;
import fr.rakambda.plexdeleter.storage.entity.MediaEntity;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.BodyInserters;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import static org.springframework.http.HttpStatus.BAD_GATEWAY;

@Slf4j
@Service
public class OverseerrApiService{
	private final RestClient apiClient;
	
	public OverseerrApiService(ApplicationConfiguration applicationConfiguration){
		apiClient = RestClient.builder()
				.baseUrl(applicationConfiguration.getOverseerr().getEndpoint())
				.defaultHeader("X-Api-Key", applicationConfiguration.getOverseerr().getApiKey())
				.requestInterceptor(new RetryInterceptor(10, 60_000, ChronoUnit.MILLIS, BAD_GATEWAY))
				.build();
	}
	
	@NonNull
	public Media getMediaDetails(int mediaId, @NonNull MediaType type) throws RequestFailedException{
		return switch(type){
			case TV -> getTvDetails(mediaId);
			case MOVIE -> getMovieDetails(mediaId);
		};
	}
	
	@NonNull
	private MovieMedia getMovieDetails(int mediaId) throws RequestFailedException{
		log.info("Getting movie details for media id {}", mediaId);
		return HttpUtils.unwrapIfStatusOkAndNotNullBody(apiClient.get()
				.uri(b -> b.pathSegment("api", "v1", "movie", "{mediaId}")
						.build(mediaId))
				.retrieve()
				.toEntity(MovieMedia.class));
	}
	
	@NonNull
	private SeriesMedia getTvDetails(int mediaId) throws RequestFailedException{
		log.info("Getting tv details for media id {}", mediaId);
		return HttpUtils.unwrapIfStatusOkAndNotNullBody(apiClient.get()
				.uri(b -> b.pathSegment("api", "v1", "tv", "{mediaId}")
						.build(mediaId))
				.retrieve()
				.toEntity(SeriesMedia.class));
	}
	
	@NonNull
	public Request getRequestDetails(int requestId) throws RequestFailedException{
		log.info("Getting request details for request id {}", requestId);
		return HttpUtils.unwrapIfStatusOkAndNotNullBody(apiClient.get()
				.uri(b -> b.pathSegment("api", "v1", "request", "{requestId}")
						.build(requestId))
				.retrieve()
				.toEntity(Request.class));
	}
	
	public void deleteRequestForUserAndMedia(@NonNull Collection<Integer> userIds, @NonNull MediaEntity media) throws RequestFailedException{
		var requestIds = new HashSet<Integer>();
		for(var userId : userIds){
			requestIds.addAll(getUserRequests(userId)
					.getResults().stream()
					.filter(request -> filterRequest(request, media))
					.map(Request::getId)
					.toList());
		}
		
		for(var requestId : requestIds){
			deleteRequest(requestId);
		}
	}
	
	private static boolean filterRequest(@NonNull Request request, @NonNull MediaEntity media){
		if(Objects.isNull(request.getMedia())){
			return false;
		}
		if(media.getType() == fr.rakambda.plexdeleter.storage.entity.MediaType.SEASON){
			if(!request.getSeasons().stream()
					.map(RequestSeason::getSeasonNumber)
					.toList()
					.contains(media.getIndex())){
				return false;
			}
		}
		
		if(Objects.nonNull(media.getTmdbId()) && Objects.equals(request.getMedia().getTmdbId(), media.getTmdbId())){
			return true;
		}
		if(Objects.nonNull(media.getTvdbId()) && Objects.equals(request.getMedia().getTvdbId(), media.getTvdbId())){
			return true;
		}
		if(Objects.nonNull(media.getPlexId()) && Objects.equals(request.getMedia().getRatingKey(), media.getPlexId())){
			return true;
		}
		return false;
	}
	
	@NonNull
	public PagedResponse<Request> getUserRequests(int userId) throws RequestFailedException{
		log.info("Getting user requests for user id {}", userId);
		return HttpUtils.unwrapIfStatusOkAndNotNullBody(apiClient.get()
				.uri(b -> b.pathSegment("api", "v1", "user", "{userId}", "requests")
						.queryParam("take", 1000)
						.build(userId))
				.retrieve()
				.toEntity(new ParameterizedTypeReference<>(){}));
	}
	
	@NonNull
	public PlexSyncResponse plexSync(boolean cancel, boolean start) throws RequestFailedException{
		var data = new PlexSyncRequest(cancel, start);
		log.info("Modifying plex sync with params {}", data);
		return HttpUtils.unwrapIfStatusOkAndNotNullBody(apiClient.post()
				.uri(b -> b.pathSegment("api", "v1", "settings", "plex", "sync").build())
				.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
				.body(BodyInserters.fromValue(data))
				.retrieve()
				.toEntity(new ParameterizedTypeReference<>(){}));
	}
	
	public void deleteRequest(int requestId) throws RequestFailedException{
		log.info("Deleting request with id {}", requestId);
		HttpUtils.requireStatusOk(apiClient.delete()
				.uri(b -> b.pathSegment("api", "v1", "request", "{requestId}")
						.build(requestId))
				.retrieve()
				.toBodilessEntity());
	}
	
	public void deleteMedia(int mediaId) throws RequestFailedException{
		log.info("Deleting media with id {}", mediaId);
		HttpUtils.requireStatusOk(apiClient.delete()
				.uri(b -> b.pathSegment("api", "v1", "media", "{mediaId}")
						.build(mediaId))
				.retrieve()
				.toBodilessEntity());
	}
}
