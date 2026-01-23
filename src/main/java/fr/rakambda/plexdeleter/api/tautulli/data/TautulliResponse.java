package fr.rakambda.plexdeleter.api.tautulli.data;

import fr.rakambda.plexdeleter.json.EmptyObjectAsNullDeserializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import tools.jackson.databind.annotation.JsonDeserialize;
import java.util.Optional;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterReflectionForBinding(TautulliResponse.class)
public class TautulliResponse<T>{
	private String result;
	private String message;
	@Nullable
	@JsonDeserialize(using = EmptyObjectAsNullDeserializer.class)
	private T data;
	
	@NonNull
	public Optional<T> getDataOptional(){
		return Optional.ofNullable(data);
	}
}
