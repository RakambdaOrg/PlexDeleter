package fr.rakambda.plexdeleter.api.servarr.radarr.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import java.util.HashSet;
import java.util.Set;

@RegisterReflectionForBinding(Movie.class)
public record Movie(
		@NonNull String title,
		boolean hasFile,
		@JsonProperty("isAvailable") boolean available,
		@Nullable String titleSlug,
		@Nullable Integer tmdbId,
		@NonNull Set<Integer> tags,
		int id
){
	public Movie{
		if(tags == null){
			tags = new HashSet<>();
		}
	}
}
