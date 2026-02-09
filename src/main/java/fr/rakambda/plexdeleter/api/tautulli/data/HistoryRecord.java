package fr.rakambda.plexdeleter.api.tautulli.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterReflectionForBinding(HistoryRecord.class)
public class HistoryRecord{
	@JsonProperty("user_id")
	private int userId;
	@Nullable
	@JsonProperty("media_index")
	private Integer mediaIndex;
	@JsonProperty("percent_complete")
	private int percentComplete;
	@JsonProperty("watched_status")
	private int watchedStatus;
}
