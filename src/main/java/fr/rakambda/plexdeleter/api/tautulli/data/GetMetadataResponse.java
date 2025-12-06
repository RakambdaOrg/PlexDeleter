package fr.rakambda.plexdeleter.api.tautulli.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import fr.rakambda.plexdeleter.json.EmptyStringAsNullDeserializer;
import lombok.ToString;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@RegisterReflectionForBinding(GetMetadataResponse.class)
public record GetMetadataResponse(
		@JsonProperty("media_type") @NonNull MediaType mediaType,
		@JsonProperty("parent_media_index") @Nullable Integer parentMediaIndex,
		@JsonProperty("media_index") @Nullable Integer mediaIndex,
		@JsonProperty("rating_key") @NonNull Integer ratingKey,
		@JsonProperty("parent_rating_key") @Nullable Integer parentRatingKey,
		@JsonProperty("grandparent_rating_key") @Nullable Integer grandparentRatingKey,
		@JsonProperty("library_name") @NonNull String libraryName,
		@JsonProperty("title") @NonNull String title,
		@JsonProperty("full_title") @NonNull String fullTitle,
		@JsonProperty("summary") @JsonDeserialize(using = EmptyStringAsNullDeserializer.class) @Nullable String summary,
		@ToString.Exclude @JsonProperty("media_info") @NonNull Set<MediaInfo> mediaInfo,
		@JsonProperty("added_at") @NonNull Instant addedAt,
		@JsonProperty("originally_available_at") @Nullable LocalDate originallyAvailableAt,
		@JsonProperty("actors") @NonNull List<String> actors,
		@JsonProperty("genres") @NonNull List<String> genres,
		@JsonProperty("duration") @Nullable Long duration,
		@JsonProperty("guid") @JsonDeserialize(using = EmptyStringAsNullDeserializer.class) @Nullable String guid,
		@JsonProperty("parent_guid") @JsonDeserialize(using = EmptyStringAsNullDeserializer.class) @Nullable String parentGuid,
		@JsonProperty("grandparent_guid") @JsonDeserialize(using = EmptyStringAsNullDeserializer.class) @Nullable String grandparentGuid,
		@JsonProperty("guids") @JsonDeserialize(contentUsing = EmptyStringAsNullDeserializer.class) @NonNull List<String> guids,
		@JsonProperty("parent_guids") @JsonDeserialize(contentUsing = EmptyStringAsNullDeserializer.class) @NonNull List<String> parentGuids,
		@JsonProperty("grandparent_guids") @JsonDeserialize(contentUsing = EmptyStringAsNullDeserializer.class) @NonNull List<String> grandparentGuids
){
	public GetMetadataResponse{
		if(actors == null){
			actors = new ArrayList<>();
		}
		if(genres == null){
			genres = new ArrayList<>();
		}
		if(guids == null){
			guids = new ArrayList<>();
		}
		if(parentGuids == null){
			parentGuids = new ArrayList<>();
		}
		if(grandparentGuids == null){
			grandparentGuids = new ArrayList<>();
		}
	}
	
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
