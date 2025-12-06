package fr.rakambda.plexdeleter.api.tvdb.data;

import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

@RegisterReflectionForBinding(Translation.class)
public record Translation(
		@Nullable String name,
		@Nullable String overview
){
}
