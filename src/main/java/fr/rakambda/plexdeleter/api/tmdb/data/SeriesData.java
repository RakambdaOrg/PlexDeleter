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
@RegisterReflectionForBinding(SeriesData.class)
public class SeriesData extends RootMediaData{
	@NonNull
	@JsonProperty("original_name")
	String originalName;
	@Nullable
	@JsonDeserialize(using = EmptyStringAsNullDeserializer.class)
	String name;
	
	public SeriesData(@Nullable String overview, @Nullable List<Genre> genres, @NonNull String originalName, @Nullable String name){
		super(overview, genres);
		this.originalName = originalName;
		this.name = name;
	}
}
