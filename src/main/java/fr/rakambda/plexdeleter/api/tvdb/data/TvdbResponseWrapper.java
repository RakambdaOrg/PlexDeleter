package fr.rakambda.plexdeleter.api.tvdb.data;

import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

@RegisterReflectionForBinding(TvdbResponseWrapper.class)
public record TvdbResponseWrapper<T>(@Nullable T data){
}
