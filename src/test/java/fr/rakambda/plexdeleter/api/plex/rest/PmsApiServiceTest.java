package fr.rakambda.plexdeleter.api.plex.rest;

import fr.rakambda.plexdeleter.SecretsUtils;
import fr.rakambda.plexdeleter.api.RequestFailedException;
import fr.rakambda.plexdeleter.config.ApplicationConfiguration;
import fr.rakambda.plexdeleter.config.PlexConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisabledIfEnvironmentVariable(named = "CI", matches = "true", disabledReason = "Required service not available on CI")
class PmsApiServiceTest{
	private PmsApiService tested;
	
	@BeforeEach
	void setUp(){
		var conf = mock(ApplicationConfiguration.class);
		var plexConf = mock(PlexConfiguration.class);
		when(conf.getPlex()).thenReturn(plexConf);
		when(plexConf.getPmsEndpoint()).thenReturn(SecretsUtils.getSecret("plex.pms.endpoint"));
		when(plexConf.getPmsToken()).thenReturn(SecretsUtils.getSecret("plex.pms.token"));
		
		tested = new PmsApiService(conf);
	}
	
	@Test
	void itShouldGetMetadata() throws RequestFailedException{
		var result = tested.getElementMetadata(832309);
		
		assertThat(result).isNotNull();
	}
	
	@Test
	void itShouldSetCollections() throws RequestFailedException{
		tested.setElementCollections(832309, List.of("Test1", "Test3"));
	}
}