package fr.rakambda.plexdeleter.api.overseerr;

import fr.rakambda.plexdeleter.api.HttpUtils;
import fr.rakambda.plexdeleter.api.RequestFailedException;
import fr.rakambda.plexdeleter.api.overseerr.data.Media;
import fr.rakambda.plexdeleter.api.overseerr.data.MediaType;
import fr.rakambda.plexdeleter.api.overseerr.data.MovieMedia;
import fr.rakambda.plexdeleter.api.overseerr.data.PagedResponse;
import fr.rakambda.plexdeleter.api.overseerr.data.Request;
import fr.rakambda.plexdeleter.api.overseerr.data.RequestSeason;
import fr.rakambda.plexdeleter.api.overseerr.data.SeriesMedia;
import fr.rakambda.plexdeleter.config.ApplicationConfiguration;
import fr.rakambda.plexdeleter.storage.entity.MediaEntity;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

@Slf4j
@Service
public class OverseerrService{
	private final WebClient apiClient;
	
	public OverseerrService(ApplicationConfiguration applicationConfiguration){
		apiClient = WebClient.builder()
				.baseUrl(applicationConfiguration.getOverseerr().getEndpoint())
				.defaultHeader("X-Api-Key", applicationConfiguration.getOverseerr().getApiKey())
				.filter(HttpUtils.logErrorFilter())
				.build();
	}
	
	@NotNull
	public Media getMediaDetails(int mediaId, @NotNull MediaType type) throws RequestFailedException{
		return switch(type){
			case TV -> getTvDetails(mediaId);
			case MOVIE -> getMovieDetails(mediaId);
		};
	}
	
	@NotNull
	private MovieMedia getMovieDetails(int mediaId) throws RequestFailedException{
		log.info("Getting movie details for media id {}", mediaId);
		return HttpUtils.unwrapIfStatusOkAndNotNullBody(apiClient.get()
				.uri(b -> b.pathSegment("api", "v1", "movie", "{mediaId}")
						.build(mediaId))
				.retrieve()
				.toEntity(MovieMedia.class)
				.blockOptional()
				.orElseThrow(() -> new RequestFailedException("Failed to get movie details with id %d".formatted(mediaId))));
	}
	
	@NotNull
	private SeriesMedia getTvDetails(int mediaId) throws RequestFailedException{
		log.info("Getting tv details for media id {}", mediaId);
		return HttpUtils.unwrapIfStatusOkAndNotNullBody(apiClient.get()
				.uri(b -> b.pathSegment("api", "v1", "tv", "{mediaId}")
						.build(mediaId))
				.retrieve()
				.toEntity(SeriesMedia.class)
				.blockOptional()
				.orElseThrow(() -> new RequestFailedException("Failed to get series details with id %d".formatted(mediaId))));
	}
	
	@NotNull
	public Request getRequestDetails(int requestId) throws RequestFailedException{
		log.info("Getting request details for request id {}", requestId);
		return HttpUtils.unwrapIfStatusOkAndNotNullBody(apiClient.get()
				.uri(b -> b.pathSegment("api", "v1", "request", "{requestId}")
						.build(requestId))
				.retrieve()
				.toEntity(Request.class)
				.blockOptional()
				.orElseThrow(() -> new RequestFailedException("Failed to get request details with id %d".formatted(requestId))));
	}
	
	public void deleteRequestForUserAndMedia(@NotNull Collection<Integer> userIds, @NotNull MediaEntity media) throws RequestFailedException{
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
	
	private static boolean filterRequest(@NotNull Request request, @NotNull MediaEntity media){
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
	
	@NotNull
	public PagedResponse<Request> getUserRequests(int userId) throws RequestFailedException{
		log.info("Getting user requests for user id {}", userId);
		return HttpUtils.unwrapIfStatusOkAndNotNullBody(apiClient.get()
				.uri(b -> b.pathSegment("api", "v1", "user", "{userId}", "requests")
						.queryParam("take", 1000)
						.build(userId))
				.retrieve()
				.toEntity(new ParameterizedTypeReference<PagedResponse<Request>>(){})
				.blockOptional()
				.orElseThrow(() -> new RequestFailedException("Failed to get request details with id %d".formatted(userId))));
	}
	
	public void deleteRequest(int requestId) throws RequestFailedException{
		log.info("Deleting request with id {}", requestId);
		HttpUtils.requireStatusOk(apiClient.delete()
				.uri(b -> b.pathSegment("api", "v1", "request", "{requestId}")
						.build(requestId))
				.retrieve()
				.toBodilessEntity()
				.blockOptional()
				.orElseThrow(() -> new RequestFailedException("Failed to delete request with id %d".formatted(requestId))));
	}
}
