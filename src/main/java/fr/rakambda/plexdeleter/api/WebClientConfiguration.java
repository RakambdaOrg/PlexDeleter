package fr.rakambda.plexdeleter.api;

import org.springframework.boot.webclient.WebClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.json.JacksonJsonDecoder;
import org.springframework.http.codec.json.JacksonJsonEncoder;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class WebClientConfiguration{
	@Bean
	public WebClientCustomizer globalCustomizer(JsonMapper jsonMapper){
		return builder -> builder
				.filter(HttpUtils.logErrorFilter())
				.codecs(codec -> {
					codec.defaultCodecs().jacksonJsonEncoder(new JacksonJsonEncoder(jsonMapper));
					codec.defaultCodecs().jacksonJsonDecoder(new JacksonJsonDecoder(jsonMapper));
					codec.defaultCodecs().maxInMemorySize(25 * 1024 * 1024);
				});
	}
}
