package fr.rakambda.plexdeleter.api.servarr.radarr;

import fr.rakambda.plexdeleter.api.HttpUtils;
import fr.rakambda.plexdeleter.api.RequestFailedException;
import fr.rakambda.plexdeleter.api.servarr.data.PagedResponse;
import fr.rakambda.plexdeleter.api.servarr.data.Tag;
import fr.rakambda.plexdeleter.api.servarr.data.UpdateMedia;
import fr.rakambda.plexdeleter.api.servarr.radarr.data.Movie;
import fr.rakambda.plexdeleter.api.servarr.radarr.data.Queue;
import fr.rakambda.plexdeleter.config.ApplicationConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;

@Slf4j
@Service
public class RadarrService{
	private final WebClient apiClient;
	
	public RadarrService(ApplicationConfiguration applicationConfiguration){
		apiClient = WebClient.builder()
				.baseUrl(applicationConfiguration.getRadarr().getEndpoint())
				.defaultHeader("X-Api-Key", applicationConfiguration.getRadarr().getApiKey())
				.build();
	}
	
	@NotNull
	public Movie getMovie(int id) throws RequestFailedException{
		log.info("Getting movie info with mediaId {}", id);
		return HttpUtils.unwrapIfStatusOkAndNotNullBody(apiClient.get()
				.uri(b -> b.pathSegment("api", "v3", "movie", "{mediaId}")
						.build(id))
				.retrieve()
				.toEntity(Movie.class)
				.blockOptional()
				.orElseThrow(() -> new RequestFailedException("Failed to get movie details with mediaId %d".formatted(id))));
	}
	
	@NotNull
	public Collection<Tag> getTags() throws RequestFailedException{
		log.info("Getting Radarr tags");
		return HttpUtils.unwrapIfStatusOkAndNotNullBody(apiClient.get()
				.uri(b -> b.pathSegment("api", "v3", "tag")
						.build())
				.retrieve()
				.toEntity(new ParameterizedTypeReference<Set<Tag>>(){})
				.blockOptional()
				.orElseThrow(() -> new RequestFailedException("Failed to get tags")));
	}
	
	public void delete(int mediaId) throws RequestFailedException{
		var queues = getQueue(mediaId).getRecords();
		for(var queue : queues){
			deleteQueue(queue.getId(), true);
		}
		deleteMovie(mediaId, true);
	}
	
	public PagedResponse<Queue> getQueue(int mediaId) throws RequestFailedException{
		log.info("Getting queue for media {}", mediaId);
		return HttpUtils.unwrapIfStatusOkAndNotNullBody(apiClient.get()
				.uri(b -> b.pathSegment("api", "v3", "queue")
						.queryParam("pageSize", 100)
						.queryParam("includeMovie", true)
						.queryParam("movieIds", mediaId)
						.build())
				.retrieve()
				.toEntity(new ParameterizedTypeReference<PagedResponse<Queue>>(){})
				.blockOptional()
				.orElseThrow(() -> new RequestFailedException("Failed to get queue for mediaId %d".formatted(mediaId))));
	}
	
	public void deleteQueue(int queueId, boolean removeFromClient) throws RequestFailedException{
		log.info("Deleting queue with id {} and removing from client {}", queueId, removeFromClient);
		HttpUtils.unwrapIfStatusOk(apiClient.delete()
				.uri(b -> b.pathSegment("api", "v3", "queue", "{mediaId}")
						.queryParam("removeFromClient", removeFromClient)
						.build(queueId))
				.retrieve()
				.toBodilessEntity()
				.blockOptional()
				.orElseThrow(() -> new RequestFailedException("Failed to delete queue with id %d".formatted(queueId))));
	}
	
	public void deleteMovie(int mediaId, boolean deleteFiles) throws RequestFailedException{
		log.info("Deleting media with mediaId {} and deleting files {}", mediaId, deleteFiles);
		HttpUtils.requireStatusOkOrNotFound(apiClient.delete()
				.uri(b -> b.pathSegment("api", "v3", "movie", "{mediaId}")
						.queryParam("deleteFiles", deleteFiles)
						.build(mediaId))
				.retrieve()
				.onStatus(HttpStatusCode::is4xxClientError, err -> Mono.empty())
				.toBodilessEntity()
				.blockOptional()
				.orElseThrow(() -> new RequestFailedException("Failed to delete media with id %d".formatted(mediaId))));
	}
	
	public void addTag(int mediaId, @NotNull String tagName) throws RequestFailedException{
		var tagId = getTags().stream()
				.filter(tag -> Objects.equals(tag.getLabel(), tagName))
				.findFirst()
				.map(Tag::getId);
		if(tagId.isEmpty()){
			log.warn("Could not find tag with label {}", tagName);
			return;
		}
		addTag(mediaId, tagId.get());
	}
	
	public void addTag(int mediaId, int tagId) throws RequestFailedException{
		var movie = getMovie(mediaId);
		movie.getTags().add(tagId);
		updateMovie(mediaId, new UpdateMedia(movie.getPath(), movie.getQualityProfileId(), movie.getTags()));
	}
	
	public void removeTag(int mediaId, @NotNull String tagName) throws RequestFailedException{
		var tagId = getTags().stream()
				.filter(tag -> Objects.equals(tag.getLabel(), tagName))
				.findFirst()
				.map(Tag::getId);
		if(tagId.isEmpty()){
			log.warn("Could not find tag with label {}", tagName);
			return;
		}
		removeTag(mediaId, tagId.get());
	}
	
	public void removeTag(int mediaId, int tagId) throws RequestFailedException{
		var movie = getMovie(mediaId);
		movie.getTags().remove(tagId);
		updateMovie(mediaId, new UpdateMedia(movie.getPath(), movie.getQualityProfileId(), movie.getTags()));
	}
	
	private void updateMovie(int mediaId, @NotNull UpdateMedia media) throws RequestFailedException{
		HttpUtils.requireStatusOk(apiClient.put()
				.uri(b -> b.pathSegment("api", "v3", "movie", "{mediaId}")
						.build(mediaId))
				.contentType(MediaType.APPLICATION_JSON)
				.body(BodyInserters.fromValue(media))
				.retrieve()
				.onStatus(HttpStatusCode::is4xxClientError, response -> response.bodyToMono(String.class)
						.handle((body, sink) -> {
							log.error("Got error with status {} and body {}", response.statusCode(), body);
							sink.error(new IllegalStateException());
						}))
				.toBodilessEntity()
				.blockOptional()
				.orElseThrow(() -> new RequestFailedException("Failed to delete media with id %d".formatted(mediaId))));
	}
}
