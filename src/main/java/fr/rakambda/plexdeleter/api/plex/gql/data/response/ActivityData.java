package fr.rakambda.plexdeleter.api.plex.gql.data.response;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NonNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "__typename")
@JsonSubTypes(value = {
		@JsonSubTypes.Type(value = ActivityWatchHistory.class, name = "ActivityWatchHistory"),
})
public sealed abstract class ActivityData permits ActivityWatchHistory{
	@NonNull
	private String id;
	@NonNull
	private MetadataItem metadataItem;
	@NonNull
	private UserV2 userV2;
}
