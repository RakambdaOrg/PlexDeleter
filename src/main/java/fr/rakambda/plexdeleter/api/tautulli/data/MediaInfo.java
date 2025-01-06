package fr.rakambda.plexdeleter.api.tautulli.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterReflectionForBinding(MediaInfo.class)
public class MediaInfo{
	@NotNull
	private Set<MediaPart> parts = new HashSet<>();
	@Nullable
	@JsonProperty("video_full_resolution")
	private String videoFullResolution;
	@Nullable
	private Long bitrate;
}
