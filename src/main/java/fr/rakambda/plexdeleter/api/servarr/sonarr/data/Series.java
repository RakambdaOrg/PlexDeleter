package fr.rakambda.plexdeleter.api.servarr.sonarr.data;

import lombok.With;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import java.util.HashSet;
import java.util.Set;

@RegisterReflectionForBinding(Series.class)
public record Series(
		@NonNull String title,
		@NonNull @With Set<Season> seasons,
		@NonNull Set<Integer> tags,
		@NonNull Integer id,
		@Nullable Integer tvdbId,
		@Nullable String titleSlug
){
	public Series{
		if(seasons == null){
			seasons = new HashSet<>();
		}
		if(tags == null){
			tags = new HashSet<>();
		}
	}
}
