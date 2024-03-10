package fr.rakambda.plexdeleter.api.servarr.sonarr.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterReflectionForBinding({Statistics.class})
public final class Statistics{
	private int episodeFileCount;
	private int episodeCount;
	private int totalEpisodeCount;
	private long sizeOnDisk;
	private float percentOfEpisodes;
	@NotNull
	private Set<String> releaseGroups = new HashSet<>();
	@Nullable
	private Instant previousAiring;
}
