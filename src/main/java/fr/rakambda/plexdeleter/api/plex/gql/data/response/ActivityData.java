package fr.rakambda.plexdeleter.api.plex.gql.data.response;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import org.jspecify.annotations.NonNull;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "__typename")
@JsonSubTypes(value = {
		@JsonSubTypes.Type(value = ActivityWatchHistory.class, name = "ActivityWatchHistory"),
})
@RegisterReflectionForBinding(ActivityData.class)
public sealed class ActivityData permits ActivityWatchHistory{
	@NonNull
	private MetadataItem metadataItem;
	@NonNull
	private UserV2 userV2;
}
