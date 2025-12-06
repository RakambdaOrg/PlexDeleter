package fr.rakambda.plexdeleter.api.tmdb.data;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import fr.rakambda.plexdeleter.json.EmptyStringAsNullDeserializer;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

@Data
@RequiredArgsConstructor
@RegisterReflectionForBinding(MediaData.class)
public sealed abstract class MediaData permits RootMediaData, SeasonData, EpisodeData{
	@Nullable
	@JsonDeserialize(using = EmptyStringAsNullDeserializer.class)
	private final String overview;
}
