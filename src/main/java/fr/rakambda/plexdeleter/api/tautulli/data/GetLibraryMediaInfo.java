package fr.rakambda.plexdeleter.api.tautulli.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterReflectionForBinding(GetLibraryMediaInfo.class)
public class GetLibraryMediaInfo{
	private int recordsFiltered;
	private int recordsTotal;
	private int draw;
	@NonNull
	@JsonProperty("filtered_file_size")
	private String filteredFileSize;
	@NonNull
	@JsonProperty("total_file_size")
	private String totalFileSize;
	@Nullable
	@JsonProperty("last_refreshed")
	private String lastRefreshed;
	@NonNull
	private Set<MediaRecord> data = new HashSet<>();
}
