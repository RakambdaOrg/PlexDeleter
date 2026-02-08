package fr.rakambda.plexdeleter.api.plex.gql;

import fr.rakambda.plexdeleter.api.ClientLoggerRequestInterceptor;
import fr.rakambda.plexdeleter.api.RequestFailedException;
import fr.rakambda.plexdeleter.config.PlexConfiguration;
import fr.rakambda.plexdeleter.json.JacksonConfiguration;
import fr.rakambda.plexdeleter.service.GraphQlService;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test")
@SpringBootTest(classes = {
		PlexCommunityApiService.class,
		GraphQlService.class,
		ClientLoggerRequestInterceptor.class,
		JacksonConfiguration.class
})
@DisabledIfEnvironmentVariable(named = "CI", matches = "true", disabledReason = "Required service not available on CI")
@EnableConfigurationProperties(PlexConfiguration.class)
@ExtendWith(MockitoExtension.class)
class PlexCommunityServiceTest{
	
	@Autowired
	private PlexCommunityApiService tested;
	
	@Test
	void itShouldGetHistoryForItem() throws RequestFailedException{
		var result = tested.listActivityForItem("602e58f59b7e9c002d6feda0", null);
		
		assertThat(result).isNotEmpty();
	}
	
	@Test
	void itShouldGetHistoryForUnknownItem(){
		assertThatThrownBy(() -> tested.listActivityForItem("123", null))
				.isInstanceOf(RequestFailedException.class)
				.hasMessage("An internal server error occurred.");
	}
}