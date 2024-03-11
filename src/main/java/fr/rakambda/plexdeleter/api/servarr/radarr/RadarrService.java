package fr.rakambda.plexdeleter.api.servarr.radarr;

import fr.rakambda.plexdeleter.api.HttpUtils;
import fr.rakambda.plexdeleter.api.RequestFailedException;
import fr.rakambda.plexdeleter.api.servarr.data.Tag;
import fr.rakambda.plexdeleter.api.servarr.radarr.data.Movie;
import fr.rakambda.plexdeleter.config.ApplicationConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.Collection;
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
		log.info("Getting movie info with id {}", id);
		return HttpUtils.withStatusOkAndBody(apiClient.get()
				.uri(b -> b.pathSegment("api", "v3", "movie", "{id}")
						.build(id))
				.retrieve()
				.toEntity(Movie.class)
				.blockOptional()
				.orElseThrow(() -> new RequestFailedException("Failed to get movie details with id %d".formatted(id))));
	}
	
	@NotNull
	public Collection<Tag> getTags() throws RequestFailedException{
		log.info("Getting Radarr tags");
		return HttpUtils.withStatusOkAndBody(apiClient.get()
				.uri(b -> b.pathSegment("api", "v3", "tag")
						.build())
				.retrieve()
				.toEntity(new ParameterizedTypeReference<Set<Tag>>(){})
				.blockOptional()
				.orElseThrow(() -> new RequestFailedException("Failed to get tags")));
	}
}
