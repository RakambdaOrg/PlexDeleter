package fr.rakambda.plexdeleter.api.plex.gql.data.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NonNull;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QueryData<T>{
	@NonNull
	private List<T> nodes = new ArrayList<>();
	@NonNull
	private PageInfo pageInfo;
}
