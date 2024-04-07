package fr.rakambda.plexdeleter.api.tautulli.data;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import fr.rakambda.plexdeleter.json.EmptyObjectAsNullDeserializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
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
	
	@NotNull
	public Optional<T> getDataOptional(){
		return Optional.ofNullable(data);
	}
}
