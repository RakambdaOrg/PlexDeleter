package fr.rakambda.plexdeleter.api.tvdb.data;

import org.jspecify.annotations.NonNull;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

@RegisterReflectionForBinding(Genre.class)
public record Genre(
		@NonNull String name,
		@NonNull String slug
){
}
