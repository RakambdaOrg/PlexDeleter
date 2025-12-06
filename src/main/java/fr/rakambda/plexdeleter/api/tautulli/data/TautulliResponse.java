package fr.rakambda.plexdeleter.api.tautulli.data;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import fr.rakambda.plexdeleter.json.EmptyObjectAsNullDeserializer;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import java.util.Optional;

@RegisterReflectionForBinding(TautulliResponse.class)
public record TautulliResponse<T>(@JsonDeserialize(using = EmptyObjectAsNullDeserializer.class) @Nullable T data){
	@NonNull
	public Optional<T> getDataOptional(){
		return Optional.ofNullable(data);
	}
}
