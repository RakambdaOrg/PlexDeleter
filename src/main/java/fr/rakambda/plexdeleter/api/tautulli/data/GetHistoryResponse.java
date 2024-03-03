package fr.rakambda.plexdeleter.api.tautulli.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
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
	@NonNull
	private Set<HistoryRecord> data = new HashSet<>();
}
