package fr.rakambda.plexdeleter.api.servarr.sonarr.data;

import fr.rakambda.plexdeleter.json.InstantAsStringWithoutNanosSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import tools.jackson.databind.annotation.JsonSerialize;
import java.time.Instant;
import java.util.HashSet;
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
	@NonNull
	private Set<String> releaseGroups = new HashSet<>();
	@Nullable
	@JsonSerialize(using = InstantAsStringWithoutNanosSerializer.class)
	private Instant previousAiring;
}
