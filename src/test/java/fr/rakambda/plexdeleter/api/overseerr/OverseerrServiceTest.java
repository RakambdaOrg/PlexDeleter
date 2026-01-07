package fr.rakambda.plexdeleter.api.overseerr;

import fr.rakambda.plexdeleter.SecretsUtils;
import fr.rakambda.plexdeleter.api.RequestFailedException;
import fr.rakambda.plexdeleter.api.overseerr.data.MediaType;
import fr.rakambda.plexdeleter.api.overseerr.data.MovieMedia;
import fr.rakambda.plexdeleter.api.overseerr.data.SeriesMedia;
import fr.rakambda.plexdeleter.config.ApplicationConfiguration;
import fr.rakambda.plexdeleter.config.OverseerrConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisabledIfEnvironmentVariable(named = "CI", matches = "true", disabledReason = "Required service not available on CI")
class OverseerrServiceTest{
	private OverseerrApiService tested;
	
	@BeforeEach
	void setUp(){
		var conf = mock(ApplicationConfiguration.class);
		when(conf.getOverseerr()).thenReturn(new OverseerrConfiguration(SecretsUtils.getSecret("overseerr.endpoint"), SecretsUtils.getSecret("overseerr.api-key")));
		
		tested = new OverseerrApiService(conf);
	}
	
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
		var result = tested.getMediaDetails(1, MediaType.TV);
		
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
		var result = tested.getMediaDetails(66038, MediaType.TV);
		
		assertThat(result.getMediaInfo()).isNotNull().satisfies(mediaInfo ->
				assertThat(mediaInfo.getRatingKey()).isEqualTo(391959));
	}
	
	@Test
	void itShouldGetRequestDetailsForMovie() throws RequestFailedException{
		var result = tested.getRequestDetails(534);
		
		assertThat(result.getMedia()).isInstanceOfSatisfying(MovieMedia.class,
				movieMedia -> assertThat(movieMedia.getId()).isEqualTo(660));
	}
	
	@Test
	void itShouldGetRequestDetailsForSeries() throws RequestFailedException{
		var result = tested.getRequestDetails(530);
		
		assertThat(result.getMedia()).isInstanceOfSatisfying(SeriesMedia.class,
				movieMedia -> assertThat(movieMedia.getId()).isEqualTo(686));
	}
	
	@Test
	void isShouldNotProduceDataBufferLimitException(){
		assertThatCode(() -> tested.getMediaDetails(4614, MediaType.TV))
				.doesNotThrowAnyException();
	}
}