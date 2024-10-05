package fr.rakambda.plexdeleter.web.webhook.tautulli.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import fr.rakambda.plexdeleter.api.tautulli.data.MediaType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.lang.NonNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TautulliWebhook{
	@NonNull
	@NotNull
	private String type;
	@NotNull
	@JsonProperty("media_type")
	private MediaType mediaType;
	@Nullable
	@JsonProperty("user_id")
	private Integer userId;
	@Nullable
	private String title;
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
}
