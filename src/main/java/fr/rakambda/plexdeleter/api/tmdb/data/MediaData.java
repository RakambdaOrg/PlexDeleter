package fr.rakambda.plexdeleter.api.tmdb.data;

import fr.rakambda.plexdeleter.json.EmptyStringAsNullDeserializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import tools.jackson.databind.annotation.JsonDeserialize;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterReflectionForBinding(MediaData.class)
public sealed abstract class MediaData permits RootMediaData, SeasonData, EpisodeData{
	@NonNull
	private Integer id;
	@Nullable
	@JsonDeserialize(using = EmptyStringAsNullDeserializer.class)
	private String overview;
}
