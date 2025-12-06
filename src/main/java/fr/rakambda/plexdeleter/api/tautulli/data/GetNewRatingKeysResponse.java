package fr.rakambda.plexdeleter.api.tautulli.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

@RegisterReflectionForBinding(GetNewRatingKeysResponse.class)
public record GetNewRatingKeysResponse(@JsonProperty("0") @Nullable GetNewRatingKeysData data){
}
