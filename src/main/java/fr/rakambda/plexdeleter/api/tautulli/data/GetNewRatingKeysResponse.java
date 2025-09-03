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
@RegisterReflectionForBinding(GetNewRatingKeysResponse.class)
public class GetNewRatingKeysResponse{
	@JsonProperty("0")
	@NonNull
	private GetNewRatingKeysData data;
	@JsonProperty("section_id")
	@Nullable
	private String sectionId;
	@JsonProperty("library_name")
	@Nullable
	private String libraryName;
}
