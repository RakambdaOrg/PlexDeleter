package fr.rakambda.plexdeleter.api.plex.gql.data.response;

import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

@RegisterReflectionForBinding(GqlError.class)
public record GqlError(String message){
}
