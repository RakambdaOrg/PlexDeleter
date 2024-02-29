package fr.rakambda.plexdeleter.api.radarr;

import fr.rakambda.plexdeleter.SecretsUtils;
import fr.rakambda.plexdeleter.api.RequestFailedException;
import fr.rakambda.plexdeleter.config.ApplicationConfiguration;
import fr.rakambda.plexdeleter.config.RadarrConfiguration;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class RadarrServiceTest{
	private RadarrService tested = new RadarrService(
			new ApplicationConfiguration(
					null,
					null,
					null,
					new RadarrConfiguration(SecretsUtils.getSecret("radarr.endpoint"), SecretsUtils.getSecret("radarr.api-key")),
					null
			));
	
	@Test
	void getMovie() throws RequestFailedException{
		var result = tested.getMovie(20);
		
		assertThat(result.getId()).isEqualTo(20);
		assertThat(result.getTitle()).isEqualTo("Monsters, Inc.");
		assertThat(result.isAvailable()).isTrue();
	}
}