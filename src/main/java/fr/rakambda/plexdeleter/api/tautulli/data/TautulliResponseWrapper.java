package fr.rakambda.plexdeleter.api.tautulli.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterReflectionForBinding(TautulliResponseWrapper.class)
public class TautulliResponseWrapper<T>{
	private TautulliResponse<T> response;
}
