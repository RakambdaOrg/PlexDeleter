package fr.rakambda.plexdeleter.api.plex.gql.data.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public final class GqlResponse<T>{
	@Nullable
	private T data;
	@Nullable
	private List<GqlError> errors = new ArrayList<>();
}
