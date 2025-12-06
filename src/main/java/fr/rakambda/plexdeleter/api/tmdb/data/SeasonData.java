package fr.rakambda.plexdeleter.api.tmdb.data;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import fr.rakambda.plexdeleter.json.EmptyStringAsNullDeserializer;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import java.util.List;
import java.util.Optional;

@Value
@EqualsAndHashCode(callSuper = true)
@RegisterReflectionForBinding(SeasonData.class)
public class SeasonData extends MediaData{
	@Nullable
	@JsonDeserialize(using = EmptyStringAsNullDeserializer.class)
	String name;
	@NonNull List<EpisodeData> episodes;
	
	public SeasonData(@Nullable String overview, @Nullable String name, @Nullable List<EpisodeData> episodes){
		super(overview);
		this.name = name;
		this.episodes = Optional.ofNullable(episodes).orElseGet(List::of);
	}
}
