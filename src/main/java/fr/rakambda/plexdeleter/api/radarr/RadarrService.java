package fr.rakambda.plexdeleter.api.radarr;

import fr.rakambda.plexdeleter.api.HttpUtils;
import fr.rakambda.plexdeleter.api.RequestFailedException;
import fr.rakambda.plexdeleter.api.radarr.data.Movie;
import fr.rakambda.plexdeleter.config.ApplicationConfiguration;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

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
		return HttpUtils.withStatusOkAndBody(apiClient.get()
				.uri(b -> b.pathSegment("api", "v3", "movie", "{id}")
						.build(id))
				.retrieve()
				.toEntity(Movie.class)
				.blockOptional()
				.orElseThrow(() -> new RequestFailedException("Failed to get movie details with id %d".formatted(id))));
	}
}
