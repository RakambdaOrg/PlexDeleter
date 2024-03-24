package fr.rakambda.plexdeleter.api.servarr.sonarr;

import fr.rakambda.plexdeleter.api.HttpUtils;
import fr.rakambda.plexdeleter.api.RequestFailedException;
import fr.rakambda.plexdeleter.api.servarr.data.PagedResponse;
import fr.rakambda.plexdeleter.api.servarr.data.Tag;
import fr.rakambda.plexdeleter.api.servarr.sonarr.data.Queue;
import fr.rakambda.plexdeleter.api.servarr.sonarr.data.Series;
import fr.rakambda.plexdeleter.config.ApplicationConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
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
public class SonarrService{
	private final WebClient apiClient;
	
	@Autowired
	public SonarrService(ApplicationConfiguration applicationConfiguration){
		apiClient = WebClient.builder()
				.clientConnector(HttpUtils.wiretapClientConnector(this.getClass()))
				.baseUrl(applicationConfiguration.getSonarr().getEndpoint())
				.defaultHeader("X-Api-Key", applicationConfiguration.getSonarr().getApiKey())
				.filter(HttpUtils.logErrorFilter())
				.build();
	}
	
	@NotNull
	public Series getSeries(int id) throws RequestFailedException{
		log.info("Getting series info with id {}", id);
		return HttpUtils.unwrapIfStatusOkAndNotNullBody(apiClient.get()
				.uri(b -> b.pathSegment("api", "v3", "series", "{id}")
						.build(id))
				.retrieve()
				.toEntity(Series.class)
				.blockOptional()
				.orElseThrow(() -> new RequestFailedException("Failed to get series details with id %d".formatted(id))));
	}
	
	@NotNull
	public Collection<Tag> getTags() throws RequestFailedException{
		log.info("Getting Sonarr tags");
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
		deleteSeries(mediaId, true);
	}
	
	public PagedResponse<Queue> getQueue(int mediaId) throws RequestFailedException{
		log.info("Getting queue for media {}", mediaId);
		return HttpUtils.unwrapIfStatusOkAndNotNullBody(apiClient.get()
				.uri(b -> b.pathSegment("api", "v3", "queue")
						.queryParam("pageSize", 100)
						.queryParam("includeSeries", true)
						.queryParam("seriesIds", mediaId)
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
	
	public void deleteSeries(int mediaId, boolean deleteFiles) throws RequestFailedException{
		log.info("Deleting media with mediaId {} and deleting files {}", mediaId, deleteFiles);
		HttpUtils.requireStatusOkOrNotFound(apiClient.delete()
				.uri(b -> b.pathSegment("api", "v3", "series", "{mediaId}")
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
		var series = getSeries(mediaId);
		if(series.getTags().add(tagId)){
			updateSeries(mediaId, series);
		}
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
		var series = getSeries(mediaId);
		if(series.getTags().remove(tagId)){
			updateSeries(mediaId, series);
		}
	}
	
	private void updateSeries(int mediaId, @NotNull Series media) throws RequestFailedException{
		HttpUtils.requireStatusOk(apiClient.put()
				.uri(b -> b.pathSegment("api", "v3", "series", "{mediaId}")
						.build(mediaId))
				.contentType(MediaType.APPLICATION_JSON)
				.body(BodyInserters.fromValue(media))
				.retrieve()
				.toBodilessEntity()
				.blockOptional()
				.orElseThrow(() -> new RequestFailedException("Failed to update media with id %d".formatted(mediaId))));
	}
}
