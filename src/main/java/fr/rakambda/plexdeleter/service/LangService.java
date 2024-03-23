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
import java.util.stream.Stream;

@Service
public class LangService{
	private final Map<String, Locale> languages;
	
	public LangService(){
		this.languages = new HashMap<>();
		for(var locale : Locale.getAvailableLocales()){
			this.languages.put(locale.getISO3Language(), locale);
		}
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
