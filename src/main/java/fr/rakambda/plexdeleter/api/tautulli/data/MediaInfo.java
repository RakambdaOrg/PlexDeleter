package fr.rakambda.plexdeleter.api.tautulli.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import java.util.HashSet;
import java.util.Set;

@RegisterReflectionForBinding(MediaInfo.class)
public record MediaInfo(
		@NonNull Set<MediaPart> parts,
		@JsonProperty("video_full_resolution") @Nullable String videoFullResolution,
		@Nullable Long bitrate
){
	public MediaInfo{
		if(parts == null){
			parts = new HashSet<>();
		}
	}
}
