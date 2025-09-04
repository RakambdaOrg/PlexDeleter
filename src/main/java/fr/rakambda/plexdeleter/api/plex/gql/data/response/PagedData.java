package fr.rakambda.plexdeleter.api.plex.gql.data.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NonNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PagedData<T>{
	@NonNull
	private QueryData<T> query;
}
