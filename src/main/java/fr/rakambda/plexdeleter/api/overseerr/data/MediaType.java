package fr.rakambda.plexdeleter.api.overseerr.data;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

@Getter
@RequiredArgsConstructor
@RegisterReflectionForBinding(MediaType.class)
public enum MediaType{
	MOVIE("movie"),
	TV("tv");
	
	@JsonValue
	private final String value;
}
