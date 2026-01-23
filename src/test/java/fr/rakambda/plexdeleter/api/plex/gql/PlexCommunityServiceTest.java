package fr.rakambda.plexdeleter.api.plex.gql;

import fr.rakambda.plexdeleter.SecretsUtils;
import fr.rakambda.plexdeleter.api.RequestFailedException;
import fr.rakambda.plexdeleter.config.ApplicationConfiguration;
import fr.rakambda.plexdeleter.config.PlexConfiguration;
import fr.rakambda.plexdeleter.service.GraphQlService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import static fr.rakambda.plexdeleter.WebClientUtils.getWebClientBuilder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisabledIfEnvironmentVariable(named = "CI", matches = "true", disabledReason = "Required service not available on CI")
class PlexCommunityServiceTest{
	private PlexCommunityApiService tested;
	
	@BeforeEach
	void setUp(){
		var conf = mock(ApplicationConfiguration.class);
		var plexConf = mock(PlexConfiguration.class);
		when(conf.getPlex()).thenReturn(plexConf);
		when(plexConf.getCommunityEndpoint()).thenReturn(SecretsUtils.getSecret("plex.community.endpoint"));
		when(plexConf.getCommunityToken()).thenReturn(SecretsUtils.getSecret("plex.community.token"));
		
		var graphQlService = new GraphQlService();
		tested = new PlexCommunityApiService(graphQlService, conf, getWebClientBuilder());
	}
	
	@Test
	void itShouldGetHistoryForItem() throws RequestFailedException{
		var result = tested.listActivityForItem("602e58f59b7e9c002d6feda0", null);
		
		assertThat(result).isNotEmpty();
	}
}