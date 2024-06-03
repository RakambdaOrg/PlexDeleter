package fr.rakambda.plexdeleter.service;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

@Service
public class LangService{
	private final Map<String, Locale> languages;
	
	public LangService(){
		this.languages = new HashMap<>();
		
		Consumer<Locale> addLanguage = locale -> this.languages.put(locale.getISO3Language(), locale);
		
		addLanguage.accept(new Locale.Builder().setLanguage("ara").build());
		addLanguage.accept(Locale.GERMAN);
		addLanguage.accept(new Locale.Builder().setLanguage("ell").build());
		addLanguage.accept(Locale.ENGLISH);
		addLanguage.accept(Locale.FRENCH);
		addLanguage.accept(Locale.ITALIAN);
		addLanguage.accept(new Locale.Builder().setLanguage("ind").build());
		addLanguage.accept(Locale.JAPANESE);
		addLanguage.accept(new Locale.Builder().setLanguage("kor").build());
		addLanguage.accept(new Locale.Builder().setLanguage("msa").build());
		addLanguage.accept(new Locale.Builder().setLanguage("por").build());
		addLanguage.accept(new Locale.Builder().setLanguage("rus").build());
		addLanguage.accept(new Locale.Builder().setLanguage("spa").build());
		addLanguage.accept(new Locale.Builder().setLanguage("tha").build());
		addLanguage.accept(new Locale.Builder().setLanguage("vie").build());
		addLanguage.accept(new Locale.Builder().setLanguage("zho").build());
	}
	
	@NotNull
	public Stream<String> getLanguageName(@Nullable String code, @NotNull Locale locale){
		if(Objects.isNull(code)){
			return Stream.empty();
		}
		return Stream.of(Optional.ofNullable(languages.get(code))
				.map(l -> l.getDisplayLanguage(locale))
				.map(StringUtils::capitalize)
				.orElse(code));
	}
}
