package fr.rakambda.plexdeleter.api.tautulli.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterReflectionForBinding(GetMetadataResponse.class)
public class GetMetadataResponse{
	@JsonProperty("media_type")
	private MediaType mediaType;
	@JsonProperty("parent_media_index")
	@Nullable
	private Integer parentMediaIndex;
	@JsonProperty("media_index")
	@Nullable
	private Integer mediaIndex;
	@JsonProperty("rating_key")
	@NonNull
	private Integer ratingKey;
	@JsonProperty("parent_rating_key")
	@Nullable
	private Integer parentRatingKey;
	@JsonProperty("grandparent_rating_key")
	@Nullable
	private Integer grandparentRatingKey;
	@JsonProperty("library_name")
	@NonNull
	private String libraryName;
	@JsonProperty("title")
	@NonNull
	private String title;
	@JsonProperty("parent_title")
	@Nullable
	private String parentTitle;
	@JsonProperty("grandparent_title")
	@Nullable
	private String grandparentTitle;
	@JsonProperty("full_title")
	@NonNull
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
	@NonNull
	private Set<MediaInfo> mediaInfo = new HashSet<>();
	@JsonProperty("added_at")
	@NonNull
	private Instant addedAt;
	@JsonProperty("originally_available_at")
	@Nullable
	private LocalDate originallyAvailableAt;
	@JsonProperty("actors")
	@NonNull
	private List<String> actors = new LinkedList<>();
	@JsonProperty("genres")
	@NonNull
	private List<String> genres = new LinkedList<>();
	@JsonProperty("duration")
	private long duration;
	@JsonProperty("guid")
	@Nullable
	private String guid;
	@JsonProperty("guids")
	@NonNull
	private List<String> guids = new LinkedList<>();
	@JsonProperty("parent_guids")
	@NonNull
	private List<String> parentGuids = new LinkedList<>();
	@JsonProperty("grandparent_guids")
	@NonNull
	private List<String> grandparentGuids = new LinkedList<>();
	
	@Nullable
	public String getGuidId(){
		if(Objects.isNull(this.guid)){
			return null;
		}
		int lastIndex = guid.lastIndexOf('/');
		if(lastIndex < 0){
			return guid;
		}
		return guid.substring(lastIndex + 1);
	}
}
