package fr.rakambda.plexdeleter.api.tautulli.data;

import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

@RegisterReflectionForBinding(TautulliResponseWrapper.class)
public record TautulliResponseWrapper<T>(TautulliResponse<T> response){
}
