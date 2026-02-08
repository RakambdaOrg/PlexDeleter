package fr.rakambda.plexdeleter.api.plex.rest;

import fr.rakambda.plexdeleter.api.HttpUtils;
import fr.rakambda.plexdeleter.api.RequestFailedException;
import fr.rakambda.plexdeleter.api.plex.rest.data.User;
import fr.rakambda.plexdeleter.config.ApplicationConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
public class PlexApiService{
	private final RestClient apiClient;
	
	public PlexApiService(ApplicationConfiguration applicationConfiguration){
		apiClient = RestClient.builder()
				.baseUrl(applicationConfiguration.getPlex().getEndpoint())
				.defaultHeader(HttpHeaders.ACCEPT, MimeTypeUtils.APPLICATION_JSON_VALUE)
				.build();
	}
	
	@NonNull
	public User getUserInfo(@NonNull String authToken) throws RequestFailedException{
		log.info("Getting user info from auth token");
		return HttpUtils.unwrapIfStatusOkAndNotNullBody(apiClient.get()
				.uri(b -> b.pathSegment("api", "v2", "user").build())
				.header("X-Plex-Token", authToken)
				.retrieve()
				.toEntity(User.class));
	}
}
