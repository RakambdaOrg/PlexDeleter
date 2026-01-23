package fr.rakambda.plexdeleter.api.servarr.radarr;

import fr.rakambda.plexdeleter.SecretsUtils;
import fr.rakambda.plexdeleter.api.RequestFailedException;
import fr.rakambda.plexdeleter.config.ApplicationConfiguration;
import fr.rakambda.plexdeleter.config.RadarrConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import static fr.rakambda.plexdeleter.WebClientUtils.getWebClientBuilder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisabledIfEnvironmentVariable(named = "CI", matches = "true", disabledReason = "Required service not available on CI")
class RadarrServiceTest{
	private RadarrApiService tested;
	
	@BeforeEach
	void setUp(){
		var conf = mock(ApplicationConfiguration.class);
		when(conf.getRadarr()).thenReturn(new RadarrConfiguration(SecretsUtils.getSecret("radarr.endpoint"), SecretsUtils.getSecret("radarr.api-key")));
		
		tested = new RadarrApiService(conf, getWebClientBuilder());
	}
	
	@Test
	void getMovie() throws RequestFailedException{
		var result = tested.getMovie(20);
		
		assertThat(result.getId()).isEqualTo(20);
		assertThat(result.getTitle()).isEqualTo("Monsters, Inc.");
		assertThat(result.isAvailable()).isTrue();
	}
}