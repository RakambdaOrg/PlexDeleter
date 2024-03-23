package fr.rakambda.plexdeleter.service;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import java.util.Locale;
import java.util.stream.Stream;
import static org.assertj.core.api.Assertions.assertThat;

class LangServiceTest{
	private final LangService tested = new LangService();
	
	@ParameterizedTest
	@MethodSource("generateCases")
	void itShouldReturnLanguage(Locale locale, String code, String expected){
		assertThat(tested.getLanguageName(code, locale)).containsOnly(expected);
	}
	
	private static Stream<Arguments> generateCases(){
		return Stream.of(
				Arguments.of(Locale.FRENCH, "fra", "Français"),
				Arguments.of(Locale.ENGLISH, "fra", "French"),
				Arguments.of(Locale.FRENCH, "eng", "Anglais"),
				Arguments.of(Locale.ENGLISH, "eng", "English"),
				Arguments.of(Locale.FRENCH, "jpn", "Japonais"),
				Arguments.of(Locale.ENGLISH, "jpn", "Japanese")
		);
	}
}