package fr.rakambda.plexdeleter.api.servarr.sonarr;

import fr.rakambda.plexdeleter.SecretsUtils;
import fr.rakambda.plexdeleter.api.RequestFailedException;
import fr.rakambda.plexdeleter.config.ApplicationConfiguration;
import fr.rakambda.plexdeleter.config.SonarrConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisabledIfEnvironmentVariable(named = "CI", matches = "true", disabledReason = "Required service not available on CI")
class SonarrServiceTest{
	private SonarrService tested;
	
	@BeforeEach
	void setUp(){
		var conf = mock(ApplicationConfiguration.class);
		when(conf.getSonarr()).thenReturn(new SonarrConfiguration(SecretsUtils.getSecret("sonarr.endpoint"), SecretsUtils.getSecret("sonarr.api-key")));
		
		tested = new SonarrService(conf);
	}
	
	@Test
	void getSeries() throws RequestFailedException{
		var result = tested.getSeries(34);
		
		assertThat(result.id()).isEqualTo(34);
		assertThat(result.title()).isEqualTo("The Wakos");
		assertThat(result.seasons()).hasSizeGreaterThan(6);
	}
}