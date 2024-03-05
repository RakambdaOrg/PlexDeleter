package fr.rakambda.plexdeleter.api.overseerr;

import fr.rakambda.plexdeleter.api.HttpUtils;
import fr.rakambda.plexdeleter.api.RequestFailedException;
import fr.rakambda.plexdeleter.api.overseerr.data.Media;
import fr.rakambda.plexdeleter.api.overseerr.data.MediaType;
import fr.rakambda.plexdeleter.api.overseerr.data.MovieMedia;
import fr.rakambda.plexdeleter.api.overseerr.data.Request;
import fr.rakambda.plexdeleter.api.overseerr.data.SeriesMedia;
import fr.rakambda.plexdeleter.config.ApplicationConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
public class OverseerrService{
	private final WebClient apiClient;
	
	public OverseerrService(ApplicationConfiguration applicationConfiguration){
		apiClient = WebClient.builder()
				.baseUrl(applicationConfiguration.getOverseerr().getEndpoint())
				.defaultHeader("X-Api-Key", applicationConfiguration.getOverseerr().getApiKey())
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
		return HttpUtils.withStatusOkAndBody(apiClient.get()
				.uri(b -> b.pathSegment("api", "v1", "movie", "{mediaId}")
						.build(mediaId))
				.retrieve()
				.toEntity(MovieMedia.class)
				.blockOptional()
				.orElseThrow(() -> new RequestFailedException("Failed to get movie details with id %d".formatted(mediaId))));
	}
	
	@NotNull
	private SeriesMedia getTvDetails(int mediaId) throws RequestFailedException{
		return HttpUtils.withStatusOkAndBody(apiClient.get()
				.uri(b -> b.pathSegment("api", "v1", "tv", "{mediaId}")
						.build(mediaId))
				.retrieve()
				.toEntity(SeriesMedia.class)
				.blockOptional()
				.orElseThrow(() -> new RequestFailedException("Failed to get series details with id %d".formatted(mediaId))));
	}
	
	@NotNull
	public Request getRequestDetails(int requestId) throws RequestFailedException{
		return HttpUtils.withStatusOkAndBody(apiClient.get()
				.uri(b -> b.pathSegment("api", "v1", "request", "{requestId}")
						.build(requestId))
				.retrieve()
				.toEntity(Request.class)
				.blockOptional()
				.orElseThrow(() -> new RequestFailedException("Failed to get request details with id %d".formatted(requestId))));
	}
}
