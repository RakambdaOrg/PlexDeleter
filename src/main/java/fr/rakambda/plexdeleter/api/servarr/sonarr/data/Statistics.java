package fr.rakambda.plexdeleter.api.servarr.sonarr.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import java.time.Instant;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterReflectionForBinding(Statistics.class)
public final class Statistics{
	private int episodeFileCount;
	private int episodeCount;
	private int totalEpisodeCount;
	private long sizeOnDisk;
	private float percentOfEpisodes;
	@Nullable
	private Set<String> releaseGroups;
	@Nullable
	private Instant previousAiring;
}
