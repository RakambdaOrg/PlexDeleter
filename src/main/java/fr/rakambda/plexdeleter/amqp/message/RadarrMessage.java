
package fr.rakambda.plexdeleter.amqp.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterReflectionForBinding(RadarrMessage.class)
public final class RadarrMessage implements IAmqpMessage{
	@NonNull
	private String type;
	@JsonProperty("movie_id")
	private int movieId;
	@NotNull
	@JsonProperty("movie_title")
	private String movieTitle;
	@Nullable
	@JsonProperty("movie_tmdb_id")
	private Integer movieTmdbId;
	@Nullable
	@JsonProperty("utc_time")
	private Instant utcTime;
}
