package fr.rakambda.plexdeleter.api.tautulli.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterReflectionForBinding(GetNewRatingKeysData.class)
public class GetNewRatingKeysData{
	@JsonProperty("rating_key")
	private int ratingKey;
	@JsonProperty("children")
	@NonNull
	private Map<String, GetNewRatingKeysData> children = new HashMap<>();
}
