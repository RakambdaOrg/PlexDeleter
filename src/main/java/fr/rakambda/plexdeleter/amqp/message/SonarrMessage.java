
package fr.rakambda.plexdeleter.amqp.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import fr.rakambda.plexdeleter.json.CommaDelimitedStringToListDeserializer;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import tools.jackson.databind.annotation.JsonDeserialize;
import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterReflectionForBinding(SonarrMessage.class)
public final class SonarrMessage implements IAmqpMessage{
	@NonNull
	private String type;
	@JsonProperty("series_id")
	private int seriesId;
	@NotNull
	@JsonProperty("series_title")
	private String seriesTitle;
	@Nullable
	@JsonProperty("series_tvdb_id")
	private Integer seriesTvdbId;
	@NotNull
	@JsonProperty("episodes_season")
	private Integer episodeSeason;
	@Nullable
	@JsonProperty("episodes_episodes")
	@JsonDeserialize(using = CommaDelimitedStringToListDeserializer.class)
	private List<String> episodeEpisodes;
	@Nullable
	@JsonProperty("utc_time")
	private Instant utcTime;
}
