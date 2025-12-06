package fr.rakambda.plexdeleter.api.tvdb.data;

import lombok.Data;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import java.util.List;
import java.util.Optional;

@Data
@RegisterReflectionForBinding(MediaData.class)
public abstract class MediaData{
	@NonNull
	private final List<Genre> genres;
	
	public MediaData(@Nullable List<Genre> genres){
		this.genres = Optional.ofNullable(genres).orElseGet(List::of);
	}
}
