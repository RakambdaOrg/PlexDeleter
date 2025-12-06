package fr.rakambda.plexdeleter.amqp.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import fr.rakambda.plexdeleter.api.tautulli.data.MediaType;
import fr.rakambda.plexdeleter.json.EmptyStringAsNullDeserializer;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

@RegisterReflectionForBinding(TautulliMessage.class)
public record TautulliMessage(
		@NonNull
		String type,
		@NonNull
		@JsonProperty("media_type")
		MediaType mediaType,
		@Nullable
		@JsonProperty("rating_key")
		Integer ratingKey,
		@Nullable
		@JsonProperty("parent_rating_key")
		Integer parentRatingKey,
		@Nullable
		@JsonProperty("grandparent_rating_key")
		Integer grandparentRatingKey,
		@Nullable
		@JsonProperty("tvdb_id")
		Integer tvdbId,
		@Nullable
		@JsonProperty("tmdb_id")
		Integer tmdbId,
		@Nullable
		@JsonProperty("user_id")
		Integer userId,
		@Nullable
		@JsonDeserialize(using = EmptyStringAsNullDeserializer.class)
		String user
) implements IAmqpMessage{
	
}
