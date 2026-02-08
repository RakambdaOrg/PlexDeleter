package fr.rakambda.plexdeleter.api.plex.rest;

import fr.rakambda.plexdeleter.api.HttpUtils;
import fr.rakambda.plexdeleter.api.RequestFailedException;
import fr.rakambda.plexdeleter.api.plex.rest.data.PmsMetadata;
import fr.rakambda.plexdeleter.config.ApplicationConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.client.RestClient;
import java.util.Collection;

@Slf4j
@Service
public class PlexMediaServerApiService{
	private final RestClient apiClient;
	
	public PlexMediaServerApiService(ApplicationConfiguration applicationConfiguration){
		apiClient = RestClient.builder()
				.baseUrl(applicationConfiguration.getPlex().getPmsEndpoint())
				.defaultHeader(HttpHeaders.ACCEPT, MimeTypeUtils.APPLICATION_JSON_VALUE)
				.defaultHeader("X-Plex-Token", applicationConfiguration.getPlex().getPmsToken())
				.build();
	}
	
	@NonNull
	public PmsMetadata getElementMetadata(int ratingKey) throws RequestFailedException{
		log.info("Getting metadata of {}", ratingKey);
		return HttpUtils.unwrapIfStatusOkAndNotNullBody(apiClient.get()
				.uri(b -> b.pathSegment("library", "metadata", Integer.toString(ratingKey)).build())
				.retrieve()
				.toEntity(PmsMetadata.class));
	}
	
	public void setElementCollections(int ratingKey, @NonNull Collection<String> collections) throws RequestFailedException{
		log.info("Setting collections of {} to {}", ratingKey, collections);
		HttpUtils.unwrapIfStatusOk(apiClient.put()
				.uri(b -> {
					b = b.pathSegment("library", "metadata", Integer.toString(ratingKey))
							.queryParam("collection.locked", 1);
					
					var i = 0;
					for(var collection : collections){
						b = b.queryParam("collection[%d].tag.tag".formatted(i++), collection);
					}
					
					return b.build();
				})
				.retrieve()
				.toBodilessEntity());
	}
	
	public void setElementLabels(int ratingKey, @NonNull Collection<String> labels) throws RequestFailedException{
		log.info("Setting labels of {} to {}", ratingKey, labels);
		HttpUtils.unwrapIfStatusOk(apiClient.put()
				.uri(b -> {
					b = b.pathSegment("library", "metadata", Integer.toString(ratingKey))
							.queryParam("label.locked", 1);
					
					var i = 0;
					for(var collection : labels){
						b = b.queryParam("label[%d].tag.tag".formatted(i++), collection);
					}
					
					return b.build();
				})
				.retrieve()
				.toBodilessEntity());
	}
}
