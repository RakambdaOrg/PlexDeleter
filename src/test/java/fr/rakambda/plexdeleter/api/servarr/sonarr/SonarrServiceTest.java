package fr.rakambda.plexdeleter.api.servarr.sonarr;

import fr.rakambda.plexdeleter.SecretsUtils;
import fr.rakambda.plexdeleter.api.ClientLoggerRequestInterceptor;
import fr.rakambda.plexdeleter.api.RequestFailedException;
import fr.rakambda.plexdeleter.config.ApplicationConfiguration;
import fr.rakambda.plexdeleter.config.SonarrConfiguration;
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
		SonarrApiService.class,
		ClientLoggerRequestInterceptor.class,
		JacksonConfiguration.class
})
@DisabledIfEnvironmentVariable(named = "CI", matches = "true", disabledReason = "Required service not available on CI")
@ExtendWith(MockitoExtension.class)
class SonarrServiceTest{
	@MockitoBean
	private ApplicationConfiguration applicationConfiguration;
	@Mock
	private SonarrConfiguration sonarrConfiguration;
	
	@Autowired
	private SonarrApiService tested;
	
	@BeforeEach
	void setUp(){
		lenient().when(applicationConfiguration.getSonarr()).thenReturn(sonarrConfiguration);
		lenient().when(sonarrConfiguration.getEndpoint()).thenReturn(SecretsUtils.getSecret("sonarr.endpoint"));
		lenient().when(sonarrConfiguration.getApiKey()).thenReturn(SecretsUtils.getSecret("sonarr.api-key"));
	}
	
	@Test
	void getSeries() throws RequestFailedException{
		var result = tested.getSeries(34);
		
		assertThat(result.getId()).isEqualTo(34);
		assertThat(result.getTitle()).isEqualTo("The Wakos");
		assertThat(result.getSeasons()).hasSizeGreaterThan(6);
	}
}