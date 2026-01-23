package fr.rakambda.plexdeleter.api.servarr.sonarr;

import fr.rakambda.plexdeleter.SecretsUtils;
import fr.rakambda.plexdeleter.api.RequestFailedException;
import fr.rakambda.plexdeleter.config.ApplicationConfiguration;
import fr.rakambda.plexdeleter.config.SonarrConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import static fr.rakambda.plexdeleter.WebClientUtils.getWebClientBuilder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisabledIfEnvironmentVariable(named = "CI", matches = "true", disabledReason = "Required service not available on CI")
class SonarrServiceTest{
	private SonarrApiService tested;
	
	@BeforeEach
	void setUp(){
		var conf = mock(ApplicationConfiguration.class);
		when(conf.getSonarr()).thenReturn(new SonarrConfiguration(SecretsUtils.getSecret("sonarr.endpoint"), SecretsUtils.getSecret("sonarr.api-key")));
		
		tested = new SonarrApiService(conf, getWebClientBuilder());
	}
	
	@Test
	void getSeries() throws RequestFailedException{
		var result = tested.getSeries(34);
		
		assertThat(result.getId()).isEqualTo(34);
		assertThat(result.getTitle()).isEqualTo("The Wakos");
		assertThat(result.getSeasons()).hasSizeGreaterThan(6);
	}
}