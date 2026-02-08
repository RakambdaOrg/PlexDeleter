package fr.rakambda.plexdeleter.api.servarr.radarr;

import fr.rakambda.plexdeleter.api.ClientLoggerRequestInterceptor;
import fr.rakambda.plexdeleter.api.RequestFailedException;
import fr.rakambda.plexdeleter.config.RadarrConfiguration;
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
		RadarrApiService.class,
		ClientLoggerRequestInterceptor.class,
		JacksonConfiguration.class
})
@DisabledIfEnvironmentVariable(named = "CI", matches = "true", disabledReason = "Required service not available on CI")
@EnableConfigurationProperties(RadarrConfiguration.class)
@ExtendWith(MockitoExtension.class)
class RadarrServiceTest{
	@Autowired
	private RadarrApiService tested;
	
	@Test
	void getMovie() throws RequestFailedException{
		var result = tested.getMovie(20);
		
		assertThat(result.getId()).isEqualTo(20);
		assertThat(result.getTitle()).isEqualTo("Monsters, Inc.");
		assertThat(result.isAvailable()).isTrue();
	}
}