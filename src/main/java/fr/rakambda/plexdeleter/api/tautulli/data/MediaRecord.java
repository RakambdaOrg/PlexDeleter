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
@RegisterReflectionForBinding(MediaRecord.class)
public class MediaRecord{
	@NotNull
	@JsonProperty("section_id")
	private Integer sectionId;
	@NotNull
	@JsonProperty("section_type")
	private MediaType sectionType;
	@NotNull
	@JsonProperty("media_type")
	private MediaType mediaType;
	@JsonProperty("rating_key")
	@NotNull
	private Integer ratingKey;
	@Nullable
	@JsonProperty("grandparent_rating_key")
	private Integer grandparentRatingKey;
	@Nullable
	@JsonProperty("parent_rating_key")
	private Integer parentRatingKey;
	@NotNull
	private String title;
}
