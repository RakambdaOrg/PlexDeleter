package fr.rakambda.plexdeleter.api.plex.gql;

import fr.rakambda.plexdeleter.SecretsUtils;
import fr.rakambda.plexdeleter.api.ClientLoggerRequestInterceptor;
import fr.rakambda.plexdeleter.api.RequestFailedException;
import fr.rakambda.plexdeleter.config.ApplicationConfiguration;
import fr.rakambda.plexdeleter.config.PlexConfiguration;
import fr.rakambda.plexdeleter.json.JacksonConfiguration;
import fr.rakambda.plexdeleter.service.GraphQlService;
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
		PlexCommunityApiService.class,
		GraphQlService.class,
		ClientLoggerRequestInterceptor.class,
		JacksonConfiguration.class
})
@DisabledIfEnvironmentVariable(named = "CI", matches = "true", disabledReason = "Required service not available on CI")
@ExtendWith(MockitoExtension.class)
class PlexCommunityServiceTest{
	
	@MockitoBean
	private ApplicationConfiguration applicationConfiguration;
	@Mock
	private PlexConfiguration plexConfiguration;
	
	@Autowired
	private PlexCommunityApiService tested;
	
	@BeforeEach
	void setUp(){
		lenient().when(applicationConfiguration.getPlex()).thenReturn(plexConfiguration);
		lenient().when(plexConfiguration.getCommunityEndpoint()).thenReturn(SecretsUtils.getSecret("plex.community.endpoint"));
		lenient().when(plexConfiguration.getCommunityToken()).thenReturn(SecretsUtils.getSecret("plex.community.token"));
	}
	
	@Test
	void itShouldGetHistoryForItem() throws RequestFailedException{
		var result = tested.listActivityForItem("602e58f59b7e9c002d6feda0", null);
		
		assertThat(result).isNotEmpty();
	}
}