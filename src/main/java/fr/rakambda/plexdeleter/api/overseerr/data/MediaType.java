package fr.rakambda.plexdeleter.api.overseerr.data;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

@Getter
@RequiredArgsConstructor
@RegisterReflectionForBinding(MediaType.class)
public enum MediaType{
	MOVIE("movie", "movie"),
	TV("tv", "show");
	
	@NotNull
	@JsonValue
	private final String value;
	@NotNull
	private final String traktSearchValue;
}
