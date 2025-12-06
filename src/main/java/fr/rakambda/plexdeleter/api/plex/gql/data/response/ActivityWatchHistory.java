package fr.rakambda.plexdeleter.api.plex.gql.data.response;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import org.jspecify.annotations.NonNull;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import java.time.Instant;

@Value
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@JsonTypeName("ActivityWatchHistory")
@RegisterReflectionForBinding(ActivityWatchHistory.class)
public class ActivityWatchHistory extends ActivityData{
	@NonNull Instant date;
	
	public ActivityWatchHistory(@NonNull MetadataItem metadataItem, @NonNull UserV2 userV2, @NonNull Instant date){
		super(metadataItem, userV2);
		this.date = date;
	}
}
