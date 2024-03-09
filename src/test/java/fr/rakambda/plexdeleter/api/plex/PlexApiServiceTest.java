package fr.rakambda.plexdeleter.api.plex;

import fr.rakambda.plexdeleter.SecretsUtils;
import fr.rakambda.plexdeleter.api.RequestFailedException;
import fr.rakambda.plexdeleter.config.ApplicationConfiguration;
import fr.rakambda.plexdeleter.config.PlexConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PlexApiServiceTest{
	private PlexApiService tested;
	
	@BeforeEach
	void setUp(){
		var conf = mock(ApplicationConfiguration.class);
		when(conf.getPlex()).thenReturn(new PlexConfiguration(SecretsUtils.getSecret("plex.endpoint")));
		
		tested = new PlexApiService(conf);
	}
	
	@Test
	void name() throws RequestFailedException{
		var username = SecretsUtils.getSecret("plex.username");
		var password = SecretsUtils.getSecret("plex.password");
		
		var response = tested.authenticate(username, password, "256755");
		
		System.out.println(response);
	}
}