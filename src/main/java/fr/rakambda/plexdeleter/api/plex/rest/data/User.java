package fr.rakambda.plexdeleter.api.plex.rest.data;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

@RegisterReflectionForBinding(User.class)
public record User(
		@Nullable Integer id,
		@NonNull String username
){
}
