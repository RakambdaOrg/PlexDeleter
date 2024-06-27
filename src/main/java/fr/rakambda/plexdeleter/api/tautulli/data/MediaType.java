package fr.rakambda.plexdeleter.api.tautulli.data;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

@Getter
@RequiredArgsConstructor
@RegisterReflectionForBinding(MediaType.class)
public enum MediaType{
	ARTIST("artist", false),
	EPISODE("episode", true),
	MOVIE("movie", true),
	PHOTO("photo", false),
	SEASON("season", true),
	SHOW("show", true),
	TRACK("track", false);
	
	@JsonValue
	private final String value;
	private final boolean notifyAdded;
}
