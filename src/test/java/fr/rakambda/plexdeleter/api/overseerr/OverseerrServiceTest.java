package fr.rakambda.plexdeleter.api.overseerr;

import fr.rakambda.plexdeleter.SecretsUtils;
import fr.rakambda.plexdeleter.api.RequestFailedException;
import fr.rakambda.plexdeleter.api.overseerr.data.MovieMedia;
import fr.rakambda.plexdeleter.api.overseerr.data.SeriesMedia;
import fr.rakambda.plexdeleter.config.ApplicationConfiguration;
import fr.rakambda.plexdeleter.config.OverseerrConfiguration;
import fr.rakambda.plexdeleter.storage.entity.MediaType;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class OverseerrServiceTest{
	private OverseerrService tested = new OverseerrService(
			new ApplicationConfiguration(
					new OverseerrConfiguration(SecretsUtils.getSecret("overseerr.endpoint"), SecretsUtils.getSecret("overseerr.api-key")),
					null,
					null,
					null,
					null
			));
	
	@Test
	void itShouldGetMediaDetailsForMovieNotOnPlex() throws RequestFailedException{
		var result = tested.getMediaDetails(2, MediaType.MOVIE);
		
		assertThat(result).isNotNull().isInstanceOfSatisfying(MovieMedia.class, movie -> {
			assertThat(movie.getId()).isEqualTo(2);
			assertThat(movie.getTitle()).isEqualTo("Ariel");
			assertThat(movie.getStatus()).isEqualTo("Released");
			assertThat(movie.getExternalIds()).isNotNull().satisfies(externalIds ->
					assertThat(externalIds.getImdbId()).isEqualTo("tt0094675"));
			assertThat(movie.getMediaInfo()).isNull();
		});
	}
	
	@Test
	void itShouldGetSeriesDetailsForSeriesNotOnPlex() throws RequestFailedException{
		var result = tested.getMediaDetails(1, MediaType.SEASON);
		
		assertThat(result).isNotNull().isInstanceOfSatisfying(SeriesMedia.class, series -> {
			assertThat(series.getId()).isEqualTo(1);
			assertThat(series.getOriginalName()).isEqualTo("プライド");
			assertThat(series.getName()).isEqualTo("Pride");
			assertThat(series.getStatus()).isEqualTo("Ended");
			assertThat(series.getSeasons()).hasSize(1);
			assertThat(series.getExternalIds()).isNotNull().satisfies(externalIds -> {
				assertThat(externalIds.getImdbId()).isEqualTo("tt0416409");
				assertThat(externalIds.getTvdbId()).isEqualTo(84831L);
			});
			assertThat(series.getMediaInfo()).isNull();
		});
	}
	
	@Test
	void itShouldGetMediaDetailsForMovieOnPlex() throws RequestFailedException{
		var result = tested.getMediaDetails(585, MediaType.MOVIE);
		
		assertThat(result.getMediaInfo()).isNotNull().satisfies(mediaInfo ->
				assertThat(mediaInfo.getRatingKey()).isEqualTo(272271));
	}
	
	@Test
	void itShouldGetSeriesDetailsForSeriesOnPlex() throws RequestFailedException{
		var result = tested.getMediaDetails(66038, MediaType.SEASON);
		
		assertThat(result.getMediaInfo()).isNotNull().satisfies(mediaInfo ->
				assertThat(mediaInfo.getRatingKey()).isEqualTo(391959));
	}
}