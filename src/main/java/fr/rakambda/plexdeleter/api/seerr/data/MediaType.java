package fr.rakambda.plexdeleter.api.seerr.data;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

@Getter
@RequiredArgsConstructor
@RegisterReflectionForBinding(MediaType.class)
public enum MediaType{
	MOVIE("movie", "movie"),
	TV("tv", "show");
	
	@NonNull
	@JsonValue
	private final String value;
	@NonNull
	private final String traktSearchValue;
}
