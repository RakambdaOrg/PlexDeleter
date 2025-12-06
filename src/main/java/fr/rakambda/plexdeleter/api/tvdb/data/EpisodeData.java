package fr.rakambda.plexdeleter.api.tvdb.data;

import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

@RegisterReflectionForBinding(EpisodeData.class)
public record EpisodeData(
		int id,
		int seriesId,
		int number,
		int seasonNumber
){
}
