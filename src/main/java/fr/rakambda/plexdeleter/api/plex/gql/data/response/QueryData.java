package fr.rakambda.plexdeleter.api.plex.gql.data.response;

import org.jspecify.annotations.NonNull;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import java.util.ArrayList;
import java.util.List;

@RegisterReflectionForBinding(QueryData.class)
public record QueryData<T>(
		@NonNull List<T> nodes,
		@NonNull PageInfo pageInfo
){
	public QueryData{
		if(nodes == null){
			nodes = new ArrayList<>();
		}
	}
}
