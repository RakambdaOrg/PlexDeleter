package fr.rakambda.plexdeleter.api.tautulli;

import fr.rakambda.plexdeleter.SecretsUtils;
import fr.rakambda.plexdeleter.api.RequestFailedException;
import fr.rakambda.plexdeleter.config.ApplicationConfiguration;
import fr.rakambda.plexdeleter.config.TautulliConfiguration;
import fr.rakambda.plexdeleter.storage.entity.MediaType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TautulliServiceTest{
	private TautulliService tested;
	
	@BeforeEach
	void setUp(){
		var conf = mock(ApplicationConfiguration.class);
		when(conf.getTautulli()).thenReturn(new TautulliConfiguration(SecretsUtils.getSecret("tautulli.endpoint"), SecretsUtils.getSecret("tautulli.api-key")));
		
		tested = new TautulliService(conf);
	}
	
	@Test
	void itShouldGetMovieRatingKeys() throws RequestFailedException{
		var result = tested.getElementsRatingKeys(272271, MediaType.MOVIE);
		
		assertThat(result).containsExactlyInAnyOrder(272271);
	}
	
	@Test
	void itShouldGetSeriesRatingKeys() throws RequestFailedException{
		var result = tested.getElementsRatingKeys(392045, MediaType.SEASON);
		
		assertThat(result).containsExactlyInAnyOrder(392046, 392047, 392048, 392049, 392050, 392051, 392052, 392053, 392054, 392055, 392056, 392057, 392058, 392059, 392060, 392061);
	}
	
	@Test
	void itShouldGetSeasonRatingKey() throws RequestFailedException{
		var result = tested.getSeasonRatingKey(391959, 2);
		
		assertThat(result).contains(392028);
	}
	
	@Test
	void itShouldNotGetSeasonRatingKeyOfUnknownSeason() throws RequestFailedException{
		var result = tested.getSeasonRatingKey(391959, 9999);
		
		assertThat(result).isEmpty();
	}
	
	@Test
	void itShouldNotGetSeasonRatingKeyOfUnknownRatingKey() throws RequestFailedException{
		var result = tested.getSeasonRatingKey(999999999, 1);
		
		assertThat(result).isEmpty();
	}
	
	@Test
	void itShouldGetMetadata() throws RequestFailedException{
		var result = tested.getMetadata(272271);
		
		assertThat(result.getResponse().getData()).satisfies(data -> {
			assertThat(data.getAddedAt()).isEqualTo("2022-09-23T17:35:49Z");
			assertThat(data.getMediaInfo())
					.hasSize(1)
					.anySatisfy(mediaInfo -> assertThat(mediaInfo.getParts())
							.hasSize(1)
							.anySatisfy(mediaPart -> {
								assertThat(mediaPart.getId()).isEqualTo(862832);
								assertThat(mediaPart.getFile()).isEqualTo("/volume1/Media/Media/Keep/Movie/Monsters, Inc. (2001) {imdb-tt0198781} {tmdb-585}/Monsters Inc. (2001) {imdb-tt0198781} [Bluray-1080p][AC3 5.1][FR+EN][x265]-H4S5S.mkv");
							})
					);
		});
	}
	
	@Test
	void itShouldGetMovieHistory() throws RequestFailedException{
		var result = tested.getHistory(272271, MediaType.MOVIE, 17746770);
		
		assertThat(result.getResponse().getData()).satisfies(data -> {
			assertThat(data.getRecordsFiltered()).isEqualTo(1);
			assertThat(data.getData()).hasSize(1)
					.allSatisfy(watch -> {
						assertThat(watch.getPercentComplete()).isLessThan(50);
						assertThat(watch.getWatchedStatus()).isEqualTo(0);
					});
		});
	}
	
	@Test
	void itShouldGetSeriesHistory() throws RequestFailedException{
		var result = tested.getHistory(791642, MediaType.SEASON, 17746770);
		
		assertThat(result.getResponse().getData()).satisfies(data -> {
			assertThat(data.getRecordsFiltered()).isGreaterThanOrEqualTo(10);
			assertThat(data.getData()).hasSizeGreaterThanOrEqualTo(10)
					.allSatisfy(watch -> {
						assertThat(watch.getPercentComplete()).isGreaterThan(80);
						assertThat(watch.getWatchedStatus()).isEqualTo(1);
					});
		});
	}
	
	@Test
	void itShouldGetUnknownHistory() throws RequestFailedException{
		var result = tested.getHistory(99999999, MediaType.SEASON, 99999999);
		
		assertThat(result.getResponse().getData()).satisfies(data -> {
			assertThat(data.getRecordsFiltered()).isEqualTo(0);
			assertThat(data.getData()).isEmpty();
		});
	}
}