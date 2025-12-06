package fr.rakambda.plexdeleter.api.plex.gql.data.response;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;

public record GqlResponse<T>(
		@Nullable
		T data,
		@NonNull
		List<GqlError> errors
){
	public GqlResponse{
		if(errors == null){
			errors = new ArrayList<>();
		}
	}
}
