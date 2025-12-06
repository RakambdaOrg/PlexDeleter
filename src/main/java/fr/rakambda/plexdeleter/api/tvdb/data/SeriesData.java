package fr.rakambda.plexdeleter.api.tvdb.data;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import java.util.List;
import java.util.Optional;

@Value
@EqualsAndHashCode(callSuper = true)
@RegisterReflectionForBinding(SeriesData.class)
public class SeriesData extends MediaData{
	@NonNull List<EpisodeData> episodes;
	
	public SeriesData(@Nullable List<Genre> genres, @Nullable List<EpisodeData> episodes){
		super(genres);
		this.episodes = Optional.ofNullable(episodes).orElseGet(List::of);
	}
}
