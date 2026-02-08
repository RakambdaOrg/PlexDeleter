package fr.rakambda.plexdeleter.api.servarr.radarr;

import fr.rakambda.plexdeleter.api.ClientLoggerRequestInterceptor;
import fr.rakambda.plexdeleter.api.HttpUtils;
import fr.rakambda.plexdeleter.api.RequestFailedException;
import fr.rakambda.plexdeleter.api.RetryInterceptor;
import fr.rakambda.plexdeleter.api.servarr.data.PagedResponse;
import fr.rakambda.plexdeleter.api.servarr.data.Tag;
import fr.rakambda.plexdeleter.api.servarr.radarr.data.Movie;
import fr.rakambda.plexdeleter.api.servarr.radarr.data.Queue;
import fr.rakambda.plexdeleter.config.RadarrConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import static org.springframework.http.HttpStatus.BAD_GATEWAY;

@Slf4j
@Service
public class RadarrApiService{
	private final RestClient apiClient;
	
	public RadarrApiService(RadarrConfiguration radarrConfiguration, ClientLoggerRequestInterceptor clientLoggerRequestInterceptor){
		apiClient = RestClient.builder()
				.baseUrl(radarrConfiguration.endpoint())
				.defaultHeader("X-Api-Key", radarrConfiguration.apiKey())
				.requestInterceptor(new RetryInterceptor(10, 60_000, ChronoUnit.MILLIS, BAD_GATEWAY))
				.requestInterceptor(clientLoggerRequestInterceptor)
				.build();
	}
	
	@NonNull
	public Movie getMovie(int id) throws RequestFailedException{
		log.info("Getting movie info with mediaId {}", id);
		return HttpUtils.unwrapIfStatusOkAndNotNullBody(apiClient.get()
				.uri(b -> b.pathSegment("api", "v3", "movie", "{mediaId}")
						.build(id))
				.retrieve()
				.toEntity(Movie.class));
	}
	
	@NonNull
	public Collection<Tag> getTags() throws RequestFailedException{
		log.info("Getting Radarr tags");
		return HttpUtils.unwrapIfStatusOkAndNotNullBody(apiClient.get()
				.uri(b -> b.pathSegment("api", "v3", "tag")
						.build())
				.retrieve()
				.toEntity(new ParameterizedTypeReference<Set<Tag>>(){}));
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
				.toEntity(new ParameterizedTypeReference<>(){}));
	}
	
	public void deleteQueue(int queueId, boolean removeFromClient) throws RequestFailedException{
		log.info("Deleting queue with id {} and removing from client {}", queueId, removeFromClient);
		HttpUtils.unwrapIfStatusOk(apiClient.delete()
				.uri(b -> b.pathSegment("api", "v3", "queue", "{mediaId}")
						.queryParam("removeFromClient", removeFromClient)
						.build(queueId))
				.retrieve()
				.toBodilessEntity());
	}
	
	public void deleteMovie(int mediaId, boolean deleteFiles) throws RequestFailedException{
		log.info("Deleting media with mediaId {} and deleting files {}", mediaId, deleteFiles);
		HttpUtils.requireStatusOkOrNotFound(apiClient.delete()
				.uri(b -> b.pathSegment("api", "v3", "movie", "{mediaId}")
						.queryParam("deleteFiles", deleteFiles)
						.build(mediaId))
				.retrieve()
				.onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {})
				.toBodilessEntity());
	}
	
	public void addTag(int mediaId, @NonNull String tagName) throws RequestFailedException{
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
		if(movie.getTags().add(tagId)){
			updateMovie(mediaId, movie);
		}
	}
	
	public void removeTag(int mediaId, @NonNull String tagName) throws RequestFailedException{
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
		if(movie.getTags().remove(tagId)){
			updateMovie(mediaId, movie);
		}
	}
	
	public void unmonitor(int mediaId) throws RequestFailedException{
		var movie = getMovie(mediaId);
		movie.setMonitored(false);
		updateMovie(mediaId, movie);
	}
	
	private void updateMovie(int mediaId, @NonNull Movie media) throws RequestFailedException{
		HttpUtils.requireStatusOk(apiClient.put()
				.uri(b -> b.pathSegment("api", "v3", "movie", "{mediaId}")
						.build(mediaId))
				.contentType(MediaType.APPLICATION_JSON)
				.body(media)
				.retrieve()
				.toBodilessEntity());
	}
}
