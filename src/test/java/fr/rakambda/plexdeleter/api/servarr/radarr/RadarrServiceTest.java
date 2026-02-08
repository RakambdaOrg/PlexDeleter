package fr.rakambda.plexdeleter.api.servarr.radarr;

import fr.rakambda.plexdeleter.SecretsUtils;
import fr.rakambda.plexdeleter.api.ClientLoggerRequestInterceptor;
import fr.rakambda.plexdeleter.api.RequestFailedException;
import fr.rakambda.plexdeleter.config.ApplicationConfiguration;
import fr.rakambda.plexdeleter.config.RadarrConfiguration;
import fr.rakambda.plexdeleter.json.JacksonConfiguration;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;

@SpringBootTest(classes = {
		RadarrApiService.class,
		ClientLoggerRequestInterceptor.class,
		JacksonConfiguration.class
})
@DisabledIfEnvironmentVariable(named = "CI", matches = "true", disabledReason = "Required service not available on CI")
@ExtendWith(MockitoExtension.class)
class RadarrServiceTest{
	@MockitoBean
	private ApplicationConfiguration applicationConfiguration;
	@Mock
	private RadarrConfiguration radarrConfiguration;
	
	@Autowired
	private RadarrApiService tested;
	
	@BeforeEach
	void setUp(){
		lenient().when(applicationConfiguration.getRadarr()).thenReturn(radarrConfiguration);
		lenient().when(radarrConfiguration.getEndpoint()).thenReturn(SecretsUtils.getSecret("radarr.endpoint"));
		lenient().when(radarrConfiguration.getApiKey()).thenReturn(SecretsUtils.getSecret("radarr.api-key"));
	}
	
	@Test
	void getMovie() throws RequestFailedException{
		var result = tested.getMovie(20);
		
		assertThat(result.getId()).isEqualTo(20);
		assertThat(result.getTitle()).isEqualTo("Monsters, Inc.");
		assertThat(result.isAvailable()).isTrue();
	}
}