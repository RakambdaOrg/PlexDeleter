package fr.rakambda.plexdeleter.api.plex.gql.data.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageInfo{
	private String endCursor;
	private boolean hasNextPage;
}
