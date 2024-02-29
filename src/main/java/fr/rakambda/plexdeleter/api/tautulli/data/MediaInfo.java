package fr.rakambda.plexdeleter.api.tautulli.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MediaInfo{
	@NotNull
	private Set<MediaPart> parts = new HashSet<>();
}
