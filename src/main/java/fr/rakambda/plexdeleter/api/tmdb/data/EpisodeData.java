package fr.rakambda.plexdeleter.api.tmdb.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

@Value
@EqualsAndHashCode(callSuper = true)
@RegisterReflectionForBinding(EpisodeData.class)
public class EpisodeData extends MediaData{
	@JsonProperty("episode_number")
	int episodeNumber;
	
	public EpisodeData(@Nullable String overview, int episodeNumber){
		super(overview);
		this.episodeNumber = episodeNumber;
	}
}
