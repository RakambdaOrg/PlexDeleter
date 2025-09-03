package fr.rakambda.plexdeleter.api.plex;

import fr.rakambda.plexdeleter.api.HttpUtils;
import fr.rakambda.plexdeleter.api.RequestFailedException;
import fr.rakambda.plexdeleter.api.plex.data.User;
import fr.rakambda.plexdeleter.config.ApplicationConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
public class PlexApiService{
	private final WebClient apiClient;
	
	public PlexApiService(ApplicationConfiguration applicationConfiguration){
		apiClient = WebClient.builder()
				.baseUrl(applicationConfiguration.getPlex().getEndpoint())
				.defaultHeader(HttpHeaders.ACCEPT, MimeTypeUtils.APPLICATION_JSON_VALUE)
				.filter(HttpUtils.logErrorFilter())
				.build();
	}
	
	@NonNull
	public User getUserInfo(@NonNull String authToken) throws RequestFailedException{
		log.info("Getting user info from auth token");
		return HttpUtils.unwrapIfStatusOkAndNotNullBody(apiClient.get()
				.uri(b -> b.pathSegment("api", "v2", "user")
						.build())
				.header("X-Plex-Token", authToken)
				.retrieve()
				.toEntity(User.class)
				.blockOptional()
				.orElseThrow(() -> new RequestFailedException("Failed to get Plex user info")));
	}
}
