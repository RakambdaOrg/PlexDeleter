package fr.rakambda.plexdeleter.api.plex;

import fr.rakambda.plexdeleter.SecretsUtils;
import fr.rakambda.plexdeleter.config.ApplicationConfiguration;
import fr.rakambda.plexdeleter.config.PlexConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisabledIfEnvironmentVariable(named = "CI", matches = "true", disabledReason = "Required service not available on CI")
class PlexApiServiceTest{
	private PlexApiService tested;
	
	@BeforeEach
	void setUp(){
		var conf = mock(ApplicationConfiguration.class);
		when(conf.getPlex()).thenReturn(new PlexConfiguration(SecretsUtils.getSecret("plex.endpoint"), SecretsUtils.getSecret("plex.app-endpoint")));
		
		tested = new PlexApiService(conf);
	}
}