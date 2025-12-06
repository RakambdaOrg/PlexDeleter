package fr.rakambda.plexdeleter.api.tmdb.data;

import org.jspecify.annotations.NonNull;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

@RegisterReflectionForBinding(Genre.class)
public record Genre(
		int id,
		@NonNull String name
){
}
