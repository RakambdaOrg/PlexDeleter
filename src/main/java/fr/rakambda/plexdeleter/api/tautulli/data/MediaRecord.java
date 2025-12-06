package fr.rakambda.plexdeleter.api.tautulli.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.NonNull;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

@RegisterReflectionForBinding(MediaRecord.class)
public record MediaRecord(
		@JsonProperty("media_type") @NonNull MediaType mediaType,
		@JsonProperty("rating_key") @NonNull Integer ratingKey,
		@NonNull String title
){
}
