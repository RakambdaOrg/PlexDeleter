package fr.rakambda.plexdeleter.api.tautulli.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterReflectionForBinding(MediaInfo.class)
public class MediaInfo{
	@Nullable
	private Set<MediaPart> parts;
}
