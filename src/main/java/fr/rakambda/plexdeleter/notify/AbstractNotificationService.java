package fr.rakambda.plexdeleter.notify;

import fr.rakambda.plexdeleter.api.tautulli.data.GetMetadataResponse;
import fr.rakambda.plexdeleter.api.tautulli.data.MediaInfo;
import fr.rakambda.plexdeleter.api.tautulli.data.MediaPart;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public abstract class AbstractNotificationService{
	protected static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	
	private final Map<String, Locale> languages;
	
	protected AbstractNotificationService(){
		this.languages = new HashMap<>();
		for(var locale : Locale.getAvailableLocales()){
			this.languages.put(locale.getISO3Language(), locale);
		}
	}
	
	@NotNull
	protected <T> Stream<T> getMediaStreams(@NotNull GetMetadataResponse metadata, @NotNull Class<T> klass){
		return metadata.getMediaInfo().stream()
				.map(MediaInfo::getParts)
				.flatMap(Collection::stream)
				.map(MediaPart::getStreams)
				.flatMap(Collection::stream)
				.filter(klass::isInstance)
				.map(klass::cast);
	}
	
	@NotNull
	protected Stream<String> getLanguageName(@Nullable String code, @NotNull Locale locale){
		if(Objects.isNull(code)){
			return Stream.empty();
		}
		return Stream.of(Optional.ofNullable(languages.get(code))
				.map(l -> l.getDisplayLanguage(locale))
				.orElse(code));
	}
}
