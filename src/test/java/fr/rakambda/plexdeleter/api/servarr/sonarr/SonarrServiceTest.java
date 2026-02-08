package fr.rakambda.plexdeleter.api.servarr.sonarr;

import fr.rakambda.plexdeleter.api.ClientLoggerRequestInterceptor;
import fr.rakambda.plexdeleter.api.RequestFailedException;
import fr.rakambda.plexdeleter.config.SonarrConfiguration;
import fr.rakambda.plexdeleter.json.JacksonConfiguration;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest(classes = {
		SonarrApiService.class,
		ClientLoggerRequestInterceptor.class,
		JacksonConfiguration.class
})
@DisabledIfEnvironmentVariable(named = "CI", matches = "true", disabledReason = "Required service not available on CI")
@EnableConfigurationProperties(SonarrConfiguration.class)
@ExtendWith(MockitoExtension.class)
class SonarrServiceTest{
	@Autowired
	private SonarrApiService tested;
	
	@Test
	void getSeries() throws RequestFailedException{
		var result = tested.getSeries(90);
		
		assertThat(result.getId()).isEqualTo(90);
		assertThat(result.getTitle()).isEqualTo("The Simpsons");
		assertThat(result.getSeasons()).hasSizeGreaterThan(36);
	}
}