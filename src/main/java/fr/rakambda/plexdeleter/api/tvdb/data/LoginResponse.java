package fr.rakambda.plexdeleter.api.tvdb.data;

import org.jspecify.annotations.NonNull;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

@RegisterReflectionForBinding(LoginResponse.class)
public record LoginResponse(@NonNull String token){
}
