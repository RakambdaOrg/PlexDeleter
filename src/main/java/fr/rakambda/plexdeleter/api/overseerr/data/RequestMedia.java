package fr.rakambda.plexdeleter.api.overseerr.data;

import lombok.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

@RegisterReflectionForBinding(RequestMedia.class)
public record RequestMedia(
		int id,
		int tmdbId,
		@Nullable Integer tvdbId,
		int ratingKey,
		@NonNull MediaType mediaType
){
}
