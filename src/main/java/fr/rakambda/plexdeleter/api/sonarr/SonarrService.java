package fr.rakambda.plexdeleter.api.sonarr;

import fr.rakambda.plexdeleter.api.HttpUtils;
import fr.rakambda.plexdeleter.api.RequestFailedException;
import fr.rakambda.plexdeleter.api.sonarr.data.Series;
import fr.rakambda.plexdeleter.config.ApplicationConfiguration;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class SonarrService{
	private final WebClient apiClient;
	
	public SonarrService(ApplicationConfiguration applicationConfiguration){
		apiClient = WebClient.builder()
				.baseUrl(applicationConfiguration.getSonarr().getEndpoint())
				.defaultHeader("X-Api-Key", applicationConfiguration.getSonarr().getApiKey())
				.build();
	}
	
	@NotNull
	public Series getSeries(int id) throws RequestFailedException{
		return HttpUtils.withStatusOkAndBody(apiClient.get()
				.uri(b -> b.pathSegment("api", "v3", "series", "{id}")
						.build(id))
				.retrieve()
				.toEntity(Series.class)
				.blockOptional()
				.orElseThrow(() -> new RequestFailedException("Failed to get series details with id %d".formatted(id))));
	}
}
