package fr.rakambda.plexdeleter.api.tautulli.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetNewRatingKeysData{
	@JsonProperty("rating_key")
	private int ratingKey;
	@JsonProperty("children")
	@NotNull
	private Map<String, GetNewRatingKeysData> children = new HashMap<>();
}
