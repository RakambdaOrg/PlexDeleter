package fr.rakambda.plexdeleter.api.plex.rest;

import fr.rakambda.plexdeleter.api.ClientLoggerRequestInterceptor;
import fr.rakambda.plexdeleter.api.HttpUtils;
import fr.rakambda.plexdeleter.api.RequestFailedException;
import fr.rakambda.plexdeleter.api.plex.rest.data.User;
import fr.rakambda.plexdeleter.config.PlexConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
public class PlexApiService{
	private final RestClient apiClient;
	
	@Autowired
	public PlexApiService(PlexConfiguration plexConfiguration, ClientLoggerRequestInterceptor clientLoggerRequestInterceptor){
		apiClient = RestClient.builder()
				.baseUrl(plexConfiguration.endpoint())
				.defaultHeader(HttpHeaders.ACCEPT, MimeTypeUtils.APPLICATION_JSON_VALUE)
				.requestInterceptor(clientLoggerRequestInterceptor)
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
