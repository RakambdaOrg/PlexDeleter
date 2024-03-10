package fr.rakambda.plexdeleter.api.plex;

import fr.rakambda.plexdeleter.api.HttpUtils;
import fr.rakambda.plexdeleter.api.RequestFailedException;
import fr.rakambda.plexdeleter.api.plex.data.Pin;
import fr.rakambda.plexdeleter.api.plex.data.User;
import fr.rakambda.plexdeleter.config.ApplicationConfiguration;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.codec.xml.Jaxb2XmlDecoder;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.UUID;

@Service
public class PlexApiService{
	private final WebClient apiClient;
	private final String clientId;
	
	public PlexApiService(ApplicationConfiguration applicationConfiguration){
		clientId = UUID.randomUUID().toString();
		
		apiClient = WebClient.builder()
				.baseUrl(applicationConfiguration.getPlex().getEndpoint())
				.defaultHeader(HttpHeaders.ACCEPT, MimeTypeUtils.APPLICATION_JSON_VALUE)
				.defaultHeader("X-Plex-Product", "PlexDeleter")
				.defaultHeader("X-Plex-Version", "Plex OAuth")
				.defaultHeader("X-Plex-Client-Identifier", clientId)
				.defaultHeader("X-Plex-Model", "Plex OAuth")
				.defaultHeader("X-Plex-Platform", "Android")
				.defaultHeader("X-Plex-Platform-Version", "1")
				.defaultHeader("X-Plex-Device", "Android")
				.defaultHeader("X-Plex-Device-Name", "PlexDeleter")
				.defaultHeader("X-Plex-Device-Screen-Resolution", "1920x1080")
				.defaultHeader("X-Plex-Language", "en")
				.exchangeStrategies(ExchangeStrategies.builder()
						.codecs(configurer -> configurer.defaultCodecs().jaxb2Decoder(new Jaxb2XmlDecoder()))
						.build())
				.build();
	}
	
	@NotNull
	public Pin generatePin() throws RequestFailedException{
		return HttpUtils.withStatusOkAndBody(apiClient.post()
				.uri(b -> b.pathSegment("api", "v2", "pins")
						.queryParam("strong", true)
						.build())
				.retrieve()
				.toEntity(Pin.class)
				.blockOptional()
				.orElseThrow(() -> new RequestFailedException("Failed to generate Plex pin")));
	}
	
	@NotNull
	public Pin pollAuthToken(long id) throws RequestFailedException{
		return HttpUtils.withStatusOkAndBody(apiClient.get()
				.uri(b -> b.pathSegment("api", "v2", "pins", "{id}")
						.queryParam("strong", true)
						.build(id))
				.retrieve()
				.toEntity(Pin.class)
				.blockOptional()
				.orElseThrow(() -> new RequestFailedException("Failed to get Plex pin status")));
	}
	
	@NotNull
	public User getUserInfo(@NotNull String authToken) throws RequestFailedException{
		return HttpUtils.withStatusOkAndBody(apiClient.get()
				.uri(b -> b.pathSegment("api", "v2", "user")
						.build())
				.header("X-Plex-Token", authToken)
				.retrieve()
				.toEntity(User.class)
				.blockOptional()
				.orElseThrow(() -> new RequestFailedException("Failed to get Plex user info")));
	}
}
