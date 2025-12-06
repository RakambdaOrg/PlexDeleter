package fr.rakambda.plexdeleter.api.plex.gql.data.request;

import lombok.Builder;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import java.util.HashMap;
import java.util.Map;

@Builder
@RegisterReflectionForBinding(GraphQlRequest.class)
public record GraphQlRequest(
		String query,
		Map<String, Object> variables
){
	public GraphQlRequest{
		if(variables == null){
			variables = new HashMap<>();
		}
	}
}
