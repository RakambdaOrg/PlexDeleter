package fr.rakambda.plexdeleter.api.seerr;

import fr.rakambda.plexdeleter.api.ClientLoggerRequestInterceptor;
import fr.rakambda.plexdeleter.api.RequestFailedException;
import fr.rakambda.plexdeleter.api.seerr.data.MediaType;
import fr.rakambda.plexdeleter.api.seerr.data.MovieMedia;
import fr.rakambda.plexdeleter.api.seerr.data.RequestMedia;
import fr.rakambda.plexdeleter.api.seerr.data.SeriesMedia;
import fr.rakambda.plexdeleter.config.SeerrConfiguration;
import fr.rakambda.plexdeleter.json.JacksonConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@ActiveProfiles("test")
@SpringBootTest(classes = {
		SeerrApiService.class,
		ClientLoggerRequestInterceptor.class,
		JacksonConfiguration.class,
})
@EnableConfigurationProperties(SeerrConfiguration.class)
@DisabledIfEnvironmentVariable(named = "CI", matches = "true", disabledReason = "Required service not available on CI")
class SeerrServiceTest{
	
	@Autowired
	private SeerrApiService tested;
	
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
		var result = tested.getRequestDetails(27);
		
		assertThat(result.getMedia()).isInstanceOfSatisfying(RequestMedia.class,
				movieMedia -> assertThat(movieMedia.getTmdbId()).isEqualTo(362476));
	}
	
	@Test
	void itShouldGetRequestDetailsForSeries() throws RequestFailedException{
		var result = tested.getRequestDetails(64);
		
		assertThat(result.getMedia()).isInstanceOfSatisfying(RequestMedia.class,
				movieMedia -> assertThat(movieMedia.getTmdbId()).isEqualTo(117465));
	}
	
	@Test
	void isShouldNotProduceDataBufferLimitException(){
		assertThatCode(() -> tested.getMediaDetails(4614, MediaType.TV))
				.doesNotThrowAnyException();
	}
}