package fr.rakambda.plexdeleter.api.sonarr;

import fr.rakambda.plexdeleter.SecretsUtils;
import fr.rakambda.plexdeleter.api.RequestFailedException;
import fr.rakambda.plexdeleter.config.ApplicationConfiguration;
import fr.rakambda.plexdeleter.config.SonarrConfiguration;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class SonarrServiceTest{
	private SonarrService tested = new SonarrService(
			new ApplicationConfiguration(
					null,
					null,
					new SonarrConfiguration(SecretsUtils.getSecret("sonarr.endpoint"), SecretsUtils.getSecret("sonarr.api-key")),
					null,
					null
			));
	
	@Test
	void getSeries() throws RequestFailedException{
		var result = tested.getSeries(34);
		
		assertThat(result.getId()).isEqualTo(34);
		assertThat(result.getTitle()).isEqualTo("The Wakos");
		assertThat(result.getSeasons()).hasSizeGreaterThan(6);
	}
}