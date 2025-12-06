package fr.rakambda.plexdeleter.api.tvdb.data;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import java.util.List;

@Value
@EqualsAndHashCode(callSuper = true)
@RegisterReflectionForBinding(MovieData.class)
public class MovieData extends MediaData{
	public MovieData(@Nullable List<Genre> genres){
		super(genres);
	}
}
