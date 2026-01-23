package fr.rakambda.plexdeleter.amqp.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import fr.rakambda.plexdeleter.api.tautulli.data.MediaType;
import fr.rakambda.plexdeleter.json.EmptyStringAsNullDeserializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import tools.jackson.databind.annotation.JsonDeserialize;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterReflectionForBinding(TautulliMessage.class)
public final class TautulliMessage implements IAmqpMessage{
	@NonNull
	private String type;
	@NonNull
	@JsonProperty("media_type")
	private MediaType mediaType;
	@Nullable
	@JsonProperty("rating_key")
	private Integer ratingKey;
	@Nullable
	@JsonProperty("parent_rating_key")
	private Integer parentRatingKey;
	@Nullable
	@JsonProperty("grandparent_rating_key")
	private Integer grandparentRatingKey;
	@Nullable
	@JsonProperty("tvdb_id")
	private Integer tvdbId;
	@Nullable
	@JsonProperty("tmdb_id")
	private Integer tmdbId;
	@Nullable
	@JsonProperty("user_id")
	private Integer userId;
	@Nullable
	@JsonDeserialize(using = EmptyStringAsNullDeserializer.class)
	private String user;
	@Nullable
	@JsonProperty("utc_time")
	private Instant utcTime;
}
