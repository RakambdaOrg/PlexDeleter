package fr.rakambda.plexdeleter.api.tmdb.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import fr.rakambda.plexdeleter.json.EmptyStringAsNullDeserializer;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import java.util.List;

@Value
@EqualsAndHashCode(callSuper = true)
@RegisterReflectionForBinding(MovieData.class)
public class MovieData extends RootMediaData{
	@NonNull
	@JsonProperty("original_title")
	String originalTitle;
	@Nullable
	@JsonDeserialize(using = EmptyStringAsNullDeserializer.class)
	String title;
	
	public MovieData(@Nullable String overview, @Nullable List<Genre> genres, @NonNull String originalTitle, @Nullable String title){
		super(overview, genres);
		this.originalTitle = originalTitle;
		this.title = title;
	}
}
