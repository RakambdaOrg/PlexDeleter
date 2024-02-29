package fr.rakambda.plexdeleter.api.tautulli.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetMetadataResponse{
	@JsonProperty("media_info")
	@NotNull
	private Set<MediaInfo> mediaInfo = new HashSet<>();
	@JsonProperty("added_at")
	@NotNull
	private Instant addedAt;
}
