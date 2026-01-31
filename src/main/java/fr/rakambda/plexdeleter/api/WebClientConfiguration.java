package fr.rakambda.plexdeleter.api;

import org.springframework.boot.webclient.WebClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebClientConfiguration{
	@Bean
	public WebClientCustomizer globalCustomizer(){
		return builder -> builder
				.filter(HttpUtils.logErrorFilter())
				.codecs(codec -> codec.defaultCodecs().maxInMemorySize(25 * 1024 * 1024));
	}
}
