package fr.rakambda.plexdeleter;

import fr.rakambda.plexdeleter.api.WebClientConfiguration;
import fr.rakambda.plexdeleter.json.JacksonConfiguration;
import org.springframework.web.reactive.function.client.WebClient;
import tools.jackson.databind.json.JsonMapper;

public class WebClientUtils{
	public static WebClient.Builder getWebClientBuilder(){
		var jsonMapperBuilderCustomizer = new JacksonConfiguration().jsonMapperBuilderCustomizer();
		var jsonBuilder = JsonMapper.builder();
		jsonMapperBuilderCustomizer.customize(jsonBuilder);
		var jsonMapper = jsonBuilder.build();
		
		var webClientCustomizer = new WebClientConfiguration().globalCustomizer(jsonMapper);
		var webClientBuilder = WebClient.builder();
		webClientCustomizer.customize(webClientBuilder);
		
		return webClientBuilder;
	}
}
