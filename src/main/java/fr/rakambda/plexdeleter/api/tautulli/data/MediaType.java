package fr.rakambda.plexdeleter.api.tautulli.data;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

@Getter
@RequiredArgsConstructor
@RegisterReflectionForBinding(MediaType.class)
public enum MediaType{
	EPISODE("episode"),
	MOVIE("movie"),
	SHOW("show"),
	SEASON("season"),
	TRACK("track");
	
	@JsonValue
	private final String value;
}
