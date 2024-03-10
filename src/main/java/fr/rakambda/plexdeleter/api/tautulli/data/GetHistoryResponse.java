package fr.rakambda.plexdeleter.api.tautulli.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterReflectionForBinding(GetHistoryResponse.class)
public class GetHistoryResponse{
	private int recordsFiltered;
	private int recordsTotal;
	private int draw;
	@NonNull
	@JsonProperty("filter_duration")
	private String filterDuration;
	@NonNull
	@JsonProperty("total_duration")
	private String totalDuration;
	@Nullable
	private Set<HistoryRecord> data;
}
