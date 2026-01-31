package fr.rakambda.plexdeleter;

import fr.rakambda.plexdeleter.api.WebClientConfiguration;
import fr.rakambda.plexdeleter.json.JacksonConfiguration;
import org.springframework.web.reactive.function.client.WebClient;

public class WebClientUtils{
	public static WebClient.Builder getWebClientBuilder(){
		var jsonMapper = JacksonConfiguration.getMapper();
		var webClientCustomizer = new WebClientConfiguration().globalCustomizer(jsonMapper);
		
		var webClientBuilder = WebClient.builder();
		webClientCustomizer.customize(webClientBuilder);
		return webClientBuilder;
	}
}
