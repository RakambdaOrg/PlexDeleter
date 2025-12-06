package fr.rakambda.plexdeleter.api.tautulli.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.NonNull;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import java.util.HashMap;
import java.util.Map;

@RegisterReflectionForBinding(GetNewRatingKeysData.class)
public record GetNewRatingKeysData(
		@JsonProperty("rating_key") int ratingKey,
		@JsonProperty("children") @NonNull Map<String, GetNewRatingKeysData> children
){
	public GetNewRatingKeysData{
		if(children == null){
			children = new HashMap<>();
		}
	}
}
