package fr.rakambda.plexdeleter.api.tmdb.data;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import java.util.List;
import java.util.Optional;

@Data
@EqualsAndHashCode(callSuper = true)
@RegisterReflectionForBinding(RootMediaData.class)
public sealed abstract class RootMediaData extends MediaData permits MovieData, SeriesData{
	@NonNull
	private List<Genre> genres;
	
	public RootMediaData(@Nullable String overview, @Nullable List<Genre> genres){
		super(overview);
		this.genres = Optional.ofNullable(genres).orElseGet(List::of);
	}
}
