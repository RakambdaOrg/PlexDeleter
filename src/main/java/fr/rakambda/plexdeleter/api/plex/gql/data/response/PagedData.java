package fr.rakambda.plexdeleter.api.plex.gql.data.response;

import org.jspecify.annotations.NonNull;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

@RegisterReflectionForBinding(PagedData.class)
public record PagedData<T>(@NonNull QueryData<T> query){
}
