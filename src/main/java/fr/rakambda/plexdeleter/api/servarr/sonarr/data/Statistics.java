package fr.rakambda.plexdeleter.api.servarr.sonarr.data;

import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

@RegisterReflectionForBinding(Statistics.class)
public record Statistics(
		int episodeFileCount,
		int totalEpisodeCount
){
}
