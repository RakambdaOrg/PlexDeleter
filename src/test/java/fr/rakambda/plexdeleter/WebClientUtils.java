package fr.rakambda.plexdeleter;

import fr.rakambda.plexdeleter.json.JacksonConfiguration;
import org.springframework.http.codec.json.JacksonJsonDecoder;
import org.springframework.http.codec.json.JacksonJsonEncoder;
import org.springframework.web.reactive.function.client.WebClient;
import static fr.rakambda.plexdeleter.api.HttpUtils.logErrorFilter;

public class WebClientUtils{
	public static WebClient.Builder getWebClientBuilder(){
		var jsonMapper = JacksonConfiguration.getMapper();
		
		return WebClient.builder()
				.filter(logErrorFilter())
				.codecs(codec -> {
					codec.defaultCodecs().jacksonJsonEncoder(new JacksonJsonEncoder(jsonMapper));
					codec.defaultCodecs().jacksonJsonDecoder(new JacksonJsonDecoder(jsonMapper));
					codec.defaultCodecs().maxInMemorySize(1024 * 1024);
				});
	}
}
