package fr.rakambda.plexdeleter.api.tautulli.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

@RegisterReflectionForBinding(HistoryRecord.class)
public record HistoryRecord(
		@JsonProperty("media_index") @Nullable Integer mediaIndex,
		@JsonProperty("percent_complete") int percentComplete,
		@JsonProperty("watched_status") int watchedStatus
){
}
