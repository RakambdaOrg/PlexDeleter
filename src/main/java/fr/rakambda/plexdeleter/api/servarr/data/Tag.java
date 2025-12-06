package fr.rakambda.plexdeleter.api.servarr.data;

import org.jspecify.annotations.NonNull;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

@RegisterReflectionForBinding(Tag.class)
public record Tag(
		@NonNull String label,
		int id
){
}
