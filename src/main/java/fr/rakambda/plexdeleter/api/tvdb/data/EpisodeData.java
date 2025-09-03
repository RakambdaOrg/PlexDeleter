package fr.rakambda.plexdeleter.api.tvdb.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterReflectionForBinding(EpisodeData.class)
public class EpisodeData{
	private int id;
	private int seriesId;
	private int number;
	private int seasonNumber;
	@Nullable
	private String name;
	@Nullable
	private String overview;
}
