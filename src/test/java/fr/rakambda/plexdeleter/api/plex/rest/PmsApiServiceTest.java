package fr.rakambda.plexdeleter.api.plex.rest;

import fr.rakambda.plexdeleter.SecretsUtils;
import fr.rakambda.plexdeleter.api.ClientLoggerRequestInterceptor;
import fr.rakambda.plexdeleter.api.RequestFailedException;
import fr.rakambda.plexdeleter.config.ApplicationConfiguration;
import fr.rakambda.plexdeleter.config.PlexConfiguration;
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
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;

@SpringBootTest(classes = {
		PlexMediaServerApiService.class,
		ClientLoggerRequestInterceptor.class,
		JacksonConfiguration.class
})
@DisabledIfEnvironmentVariable(named = "CI", matches = "true", disabledReason = "Required service not available on CI")
@ExtendWith(MockitoExtension.class)
class PmsApiServiceTest{
	@MockitoBean
	private ApplicationConfiguration applicationConfiguration;
	@Mock
	private PlexConfiguration plexConfiguration;
	
	@Autowired
	private PlexMediaServerApiService tested;
	
	@BeforeEach
	void setUp(){
		lenient().when(applicationConfiguration.getPlex()).thenReturn(plexConfiguration);
		lenient().when(plexConfiguration.getPmsEndpoint()).thenReturn(SecretsUtils.getSecret("plex.pms.endpoint"));
		lenient().when(plexConfiguration.getPmsToken()).thenReturn(SecretsUtils.getSecret("plex.pms.token"));
	}
	
	@Test
	void itShouldGetMetadata() throws RequestFailedException{
		var result = tested.getElementMetadata(829685);
		
		assertThat(result).isNotNull();
	}
	
	@Test
	void itShouldSetCollections() throws RequestFailedException{
		tested.setElementCollections(829685, List.of("Test1", "Test3"));
	}
	
	@Test
	void itShouldSetLabels() throws RequestFailedException{
		tested.setElementLabels(829685, List.of("Test1", "Test3"));
	}
}