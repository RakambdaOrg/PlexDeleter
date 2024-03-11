package fr.rakambda.plexdeleter.api.tautulli.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterReflectionForBinding(HistoryRecord.class)
public class HistoryRecord{
	private int id;
	@JsonProperty("user_id")
	private int userId;
	private String user;
	@JsonProperty("friendly_name")
	@NotNull
	private String friendlyName;
	@JsonProperty("full_title")
	@NotNull
	private String fullTitle;
	@JsonProperty("parent_title")
	@Nullable
	private String parentTitle;
	@JsonProperty("grandparent_title")
	@Nullable
	private String grandparentTitle;
	@NotNull
	private String title;
	@JsonProperty("media_type")
	@NotNull
	private String mediaType;
	@JsonProperty("rating_key")
	@NotNull
	private Integer ratingKey;
	@Nullable
	@JsonProperty("grandparent_rating_key")
	private Integer grandparentRatingKey;
	@Nullable
	@JsonProperty("parent_rating_key")
	private Integer parentRatingKey;
	@JsonProperty("media_index")
	private int mediaIndex;
	@JsonProperty("parent_media_index")
	private int parentMediaIndex;
	@JsonProperty("percent_complete")
	private int percentComplete;
	@JsonProperty("watched_status")
	private int watchedStatus;
}
