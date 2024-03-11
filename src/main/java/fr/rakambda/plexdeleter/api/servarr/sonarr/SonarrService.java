package fr.rakambda.plexdeleter.api.servarr.sonarr;

import fr.rakambda.plexdeleter.api.HttpUtils;
import fr.rakambda.plexdeleter.api.RequestFailedException;
import fr.rakambda.plexdeleter.api.servarr.data.Tag;
import fr.rakambda.plexdeleter.api.servarr.sonarr.data.Series;
import fr.rakambda.plexdeleter.config.ApplicationConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.Collection;
import java.util.Set;

@Slf4j
@Service
public class SonarrService{
	private final WebClient apiClient;
	
	@Autowired
	public SonarrService(ApplicationConfiguration applicationConfiguration){
		apiClient = WebClient.builder()
				.baseUrl(applicationConfiguration.getSonarr().getEndpoint())
				.defaultHeader("X-Api-Key", applicationConfiguration.getSonarr().getApiKey())
				.build();
	}
	
	@NotNull
	public Series getSeries(int id) throws RequestFailedException{
		log.info("Getting series info with id {}", id);
		return HttpUtils.withStatusOkAndBody(apiClient.get()
				.uri(b -> b.pathSegment("api", "v3", "series", "{id}")
						.build(id))
				.retrieve()
				.toEntity(Series.class)
				.blockOptional()
				.orElseThrow(() -> new RequestFailedException("Failed to get series details with id %d".formatted(id))));
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
