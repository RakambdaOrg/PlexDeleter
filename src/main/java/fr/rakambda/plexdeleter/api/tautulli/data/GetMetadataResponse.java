package fr.rakambda.plexdeleter.api.tautulli.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import java.time.Instant;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterReflectionForBinding(GetMetadataResponse.class)
public class GetMetadataResponse{
	@JsonProperty("media_info")
	@Nullable
	private Set<MediaInfo> mediaInfo;
	@JsonProperty("added_at")
	@NotNull
	private Instant addedAt;
}
