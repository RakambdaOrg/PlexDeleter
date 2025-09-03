package fr.rakambda.plexdeleter.api.tautulli.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterReflectionForBinding(MediaRecord.class)
public class MediaRecord{
	@NonNull
	@JsonProperty("section_id")
	private Integer sectionId;
	@NonNull
	@JsonProperty("section_type")
	private MediaType sectionType;
	@NonNull
	@JsonProperty("media_type")
	private MediaType mediaType;
	@JsonProperty("rating_key")
	@NonNull
	private Integer ratingKey;
	@Nullable
	@JsonProperty("grandparent_rating_key")
	private Integer grandparentRatingKey;
	@Nullable
	@JsonProperty("parent_rating_key")
	private Integer parentRatingKey;
	@NonNull
	private String title;
}
