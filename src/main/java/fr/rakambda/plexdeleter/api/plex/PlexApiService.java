package fr.rakambda.plexdeleter.api.plex;

import fr.rakambda.plexdeleter.api.HttpUtils;
import fr.rakambda.plexdeleter.api.RequestFailedException;
import fr.rakambda.plexdeleter.api.plex.data.User;
import fr.rakambda.plexdeleter.config.ApplicationConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.codec.xml.Jaxb2XmlDecoder;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.reactive.function.BodyInserters;
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
				.defaultHeader(HttpHeaders.ACCEPT, MimeTypeUtils.APPLICATION_XML_VALUE)
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
	public User authenticate(@NotNull String username, @NotNull String password, @Nullable String otp) throws RequestFailedException{
		return HttpUtils.withStatusOkAndBody(apiClient.post()
				.uri(b -> b.pathSegment("users", "sign_in.xml")
						.build())
				.body(BodyInserters
						.fromFormData("user[login]", username)
						.with("user[password]", password)
						.with("user[verification_code]", otp)
				)
				.retrieve()
				.toEntity(User.class)
				.blockOptional()
				.orElseThrow(() -> new RequestFailedException("Failed to authenticate with Plex for %s".formatted(username))));
	}
}
