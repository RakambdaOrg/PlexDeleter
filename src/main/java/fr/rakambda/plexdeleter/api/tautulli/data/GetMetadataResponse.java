package fr.rakambda.plexdeleter.api.tautulli.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterReflectionForBinding(GetMetadataResponse.class)
public class GetMetadataResponse{
	@JsonProperty("media_type")
	@NotNull
	private String mediaType;
	@JsonProperty("parent_media_index")
	@Nullable
	private Integer parentMediaIndex;
	@JsonProperty("media_index")
	@Nullable
	private Integer mediaIndex;
	@JsonProperty("rating_key")
	@NotNull
	private Integer ratingKey;
	@JsonProperty("parent_rating_key")
	@Nullable
	private Integer parentRatingKey;
	@JsonProperty("grandparent_rating_key")
	@Nullable
	private Integer grandparentRatingKey;
	@JsonProperty("library_name")
	@NotNull
	private String libraryName;
	@JsonProperty("title")
	@NotNull
	private String title;
	@JsonProperty("parent_title")
	@Nullable
	private String parentTitle;
	@JsonProperty("grandparent_title")
	@Nullable
	private String grandparentTitle;
	@JsonProperty("full_title")
	@NotNull
	private String fullTitle;
	@JsonProperty("thumb")
	@Nullable
	private String thumb;
	@JsonProperty("summary")
	@Nullable
	private String summary;
	@JsonProperty("rating")
	@Nullable
	private Float rating;
	@ToString.Exclude
	@JsonProperty("media_info")
	@NotNull
	private Set<MediaInfo> mediaInfo = new HashSet<>();
	@JsonProperty("added_at")
	@NotNull
	private Instant addedAt;
	@JsonProperty("originally_available_at")
	@Nullable
	private LocalDate originallyAvailableAt;
	@JsonProperty("actors")
	@NotNull
	private List<String> actors = new LinkedList<>();
	@JsonProperty("genres")
	@NotNull
	private List<String> genres = new LinkedList<>();
	@JsonProperty("duration")
	private long duration;
}
