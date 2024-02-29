package fr.rakambda.plexdeleter.api.tautulli.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetNewRatingKeysResponse{
	@JsonProperty("0")
	@NotNull
	private GetNewRatingKeysData data;
	@JsonProperty("section_id")
	@Nullable
	private String sectionId;
	@JsonProperty("library_name")
	@Nullable
	private String libraryName;
}
