package fr.rakambda.plexdeleter.api.tvdb.data;

import lombok.Builder;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

@Builder
@RegisterReflectionForBinding(LoginRequest.class)
public record LoginRequest(
		@NonNull String apikey,
		@Nullable String pin
){
}
