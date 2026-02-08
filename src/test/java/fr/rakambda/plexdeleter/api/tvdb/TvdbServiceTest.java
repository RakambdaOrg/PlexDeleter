package fr.rakambda.plexdeleter.api.tvdb;

import fr.rakambda.plexdeleter.api.ClientLoggerRequestInterceptor;
import fr.rakambda.plexdeleter.api.RequestFailedException;
import fr.rakambda.plexdeleter.config.TvdbConfiguration;
import fr.rakambda.plexdeleter.json.JacksonConfiguration;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import java.util.Locale;
import java.util.stream.Stream;
import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest(classes = {
		TvdbApiService.class,
		ClientLoggerRequestInterceptor.class,
		JacksonConfiguration.class
})
@DisabledIfEnvironmentVariable(named = "CI", matches = "true", disabledReason = "Required service not available on CI")
@EnableConfigurationProperties(TvdbConfiguration.class)
@ExtendWith(MockitoExtension.class)
class TvdbServiceTest{
	@Autowired
	private TvdbApiService tested;
	
	@ParameterizedTest
	@MethodSource("generateMovieTranslationsCases")
	void itShouldGetMovieTranslations(Locale locale, String expectedTitle, String expectedOverview) throws RequestFailedException{
		var result = tested.getMovieTranslations(339249, locale);
		
		assertThat(result.getData()).isNotNull().satisfies(data -> {
			assertThat(data.getName()).isEqualTo(expectedTitle);
			assertThat(data.getOverview()).isEqualTo(expectedOverview);
		});
	}
	
	@ParameterizedTest
	@MethodSource("generateSeriesTranslationsCases")
	void itShouldGetSeriesTranslations(Locale locale, String expectedTitle, String expectedOverview) throws RequestFailedException{
		var result = tested.getSeriesTranslations(370853, locale);
		
		assertThat(result.getData()).isNotNull().satisfies(data -> {
			assertThat(data.getName()).isEqualTo(expectedTitle);
			assertThat(data.getOverview()).isEqualTo(expectedOverview);
		});
	}
	
	@ParameterizedTest
	@MethodSource("generateSeasonTranslationsCases")
	void itShouldGetSeasonTranslations(Locale locale, String expectedTitle, String expectedOverview) throws RequestFailedException{
		var result = tested.getSeasonTranslations(15688, locale);
		
		assertThat(result.getData()).isNotNull().satisfies(data -> {
			assertThat(data.getName()).isEqualTo(expectedTitle);
			assertThat(data.getOverview()).isEqualTo(expectedOverview);
		});
	}
	
	@ParameterizedTest
	@MethodSource("generateEpisodeTranslationsCases")
	void itShouldGetEpisodeTranslations(Locale locale, String expectedTitle, String expectedOverview) throws RequestFailedException{
		var result = tested.getEpisodeTranslations(9115869, locale);
		
		assertThat(result.getData()).isNotNull().satisfies(data -> {
			assertThat(data.getName()).isEqualTo(expectedTitle);
			assertThat(data.getOverview()).isEqualTo(expectedOverview);
		});
	}
	
	@Test
	void itShouldListEpisodes() throws RequestFailedException{
		var seriesId = 370853;
		var result = tested.getEpisodes(seriesId);
		
		assertThat(result.getData()).isNotNull().satisfies(data -> {
			assertThat(data.getEpisodes()).hasSize(11)
					.satisfiesOnlyOnce(episode -> {
						assertThat(episode.getId()).isEqualTo(9115869);
						assertThat(episode.getSeriesId()).isEqualTo(seriesId);
						assertThat(episode.getSeasonNumber()).isEqualTo(1);
						assertThat(episode.getNumber()).isEqualTo(1);
					})
					.satisfiesOnlyOnce(episode -> {
						assertThat(episode.getId()).isEqualTo(9549476);
						assertThat(episode.getSeriesId()).isEqualTo(seriesId);
						assertThat(episode.getSeasonNumber()).isEqualTo(1);
						assertThat(episode.getNumber()).isEqualTo(2);
					})
					.satisfiesOnlyOnce(episode -> {
						assertThat(episode.getId()).isEqualTo(9549477);
						assertThat(episode.getSeriesId()).isEqualTo(seriesId);
						assertThat(episode.getSeasonNumber()).isEqualTo(1);
						assertThat(episode.getNumber()).isEqualTo(3);
					})
					.satisfiesOnlyOnce(episode -> {
						assertThat(episode.getId()).isEqualTo(9549478);
						assertThat(episode.getSeriesId()).isEqualTo(seriesId);
						assertThat(episode.getSeasonNumber()).isEqualTo(1);
						assertThat(episode.getNumber()).isEqualTo(4);
					})
					.satisfiesOnlyOnce(episode -> {
						assertThat(episode.getId()).isEqualTo(9549479);
						assertThat(episode.getSeriesId()).isEqualTo(seriesId);
						assertThat(episode.getSeasonNumber()).isEqualTo(1);
						assertThat(episode.getNumber()).isEqualTo(5);
					})
					.satisfiesOnlyOnce(episode -> {
						assertThat(episode.getId()).isEqualTo(9549480);
						assertThat(episode.getSeriesId()).isEqualTo(seriesId);
						assertThat(episode.getSeasonNumber()).isEqualTo(1);
						assertThat(episode.getNumber()).isEqualTo(6);
					})
					.satisfiesOnlyOnce(episode -> {
						assertThat(episode.getId()).isEqualTo(9549481);
						assertThat(episode.getSeriesId()).isEqualTo(seriesId);
						assertThat(episode.getSeasonNumber()).isEqualTo(1);
						assertThat(episode.getNumber()).isEqualTo(7);
					})
					.satisfiesOnlyOnce(episode -> {
						assertThat(episode.getId()).isEqualTo(9549482);
						assertThat(episode.getSeriesId()).isEqualTo(seriesId);
						assertThat(episode.getSeasonNumber()).isEqualTo(1);
						assertThat(episode.getNumber()).isEqualTo(8);
					})
					.satisfiesOnlyOnce(episode -> {
						assertThat(episode.getId()).isEqualTo(9549483);
						assertThat(episode.getSeriesId()).isEqualTo(seriesId);
						assertThat(episode.getSeasonNumber()).isEqualTo(1);
						assertThat(episode.getNumber()).isEqualTo(9);
					})
					.satisfiesOnlyOnce(episode -> {
						assertThat(episode.getId()).isEqualTo(10258624);
						assertThat(episode.getSeriesId()).isEqualTo(seriesId);
						assertThat(episode.getSeasonNumber()).isEqualTo(0);
						assertThat(episode.getNumber()).isEqualTo(1);
					})
					.satisfiesOnlyOnce(episode -> {
						assertThat(episode.getId()).isEqualTo(10354778);
						assertThat(episode.getSeriesId()).isEqualTo(seriesId);
						assertThat(episode.getSeasonNumber()).isEqualTo(0);
						assertThat(episode.getNumber()).isEqualTo(2);
					});
		});
	}
	
	private static Stream<Arguments> generateMovieTranslationsCases(){
		return Stream.of(
				Arguments.of(Locale.ENGLISH, "Kung Fu Panda 4", "Po is set to become the new spiritual leader of the Valley of Peace, but before he can do that, he must find a successor to become the new Dragon Warrior. He appears to find one in Zhen, a fox with plenty of promising abilities but who doesn’t quite like the idea of Po training her."),
				Arguments.of(Locale.FRENCH, "Kung Fu Panda 4", "Maître Shifu annonce à Po qu'il doit devenir le nouveau Guide Spirituel de la Vallée de la Paix, et ainsi laisser sa place à un autre Guerrier Dragon. Cette nouvelle ne plaît pas du tout au panda, qui s'épanouissait dans ce rôle de protecteur. Avant de léguer son Bâton de la Sagesse à un autre, il doit accomplir une dernière mission : empêcher la Caméléone, une sorcière pouvant se métamorphoser en n'importe quelle créature, d'étendre son pouvoir jusqu'à la Vallée de la Paix. Il sera aidé dans sa tâche par Zhen, une renarde rusée et une habile voleuse, qui sait où se cache la Caméléone.")
		);
	}
	
	private static Stream<Arguments> generateSeriesTranslationsCases(){
		return Stream.of(
				Arguments.of(Locale.ENGLISH, "Masters of the Air", "During World War II, airmen risk their lives with the 100th Bomb Group, a brotherhood forged by courage, loss, and triumph."),
				Arguments.of(Locale.FRENCH, "Les maîtres de l'air", "De Steven Spielberg, Tom Hanks et Gary Goetzman, les producteurs de Frères d'armes et Band of Brothers : l'Enfer du Pacifique. Pendant la Seconde Guerre mondiale, des pilotes de chasse risquent leur vie au sein du 100e groupe de bombardement, une confrérie unie par le courage, les défaites et les victoires.")
		);
	}
	
	private static Stream<Arguments> generateSeasonTranslationsCases(){
		return Stream.of(
				Arguments.of(Locale.ENGLISH, null, "Christopher Eccleston's Doctor is wise and funny, cheeky and brave. An alien and a loner (it's difficult keeping up with friends when your day job involves flitting through time and space), his detached logic gives him a vital edge when the world is in danger. But when it comes to human relationships, he can be found wanting. That's why he needs new assistant Rose."),
				Arguments.of(Locale.ITALIAN, null, "Il Dottore di Christopher Eccleston è saggio e divertente, sfacciato e coraggioso. Un alieno e un solitario (è difficile tenere il passo con gli amici quando il tuo lavoro quotidiano implica volare nel tempo e nello spazio), la sua logica distaccata gli dà un vantaggio vitale quando il mondo è in pericolo. Ma quando si tratta di rapporti umani, può risultare carente. Ecco perché ha bisogno della nuova assistente Rose.")
		);
	}
	
	private static Stream<Arguments> generateEpisodeTranslationsCases(){
		return Stream.of(
				Arguments.of(Locale.ENGLISH, "Part One", "Led by Majors Cleven and Egan, the 100th Bomb Group arrives in England and joins the 8th Air Force’s campaign against Nazi Germany."),
				Arguments.of(Locale.FRENCH, "Première partie", "Dirigé par les majors Cleven et Egan, le 100e Groupe de bombardement rejoint la 8e Air Force en Angleterre, pour lutter contre l’Allemagne nazie.")
		);
	}
}