package fr.rakambda.plexdeleter.api.plex.gql.data.response;

import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

@RegisterReflectionForBinding(PageInfo.class)
public record PageInfo(
		String endCursor,
		boolean hasNextPage
){
}
