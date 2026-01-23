package fr.rakambda.plexdeleter.api.tmdb.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import fr.rakambda.plexdeleter.json.EmptyStringAsNullDeserializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import tools.jackson.databind.annotation.JsonDeserialize;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@RegisterReflectionForBinding(MovieData.class)
public final class MovieData extends RootMediaData{
	@NonNull
	@JsonProperty("original_title")
	private String originalTitle;
	@Nullable
	@JsonDeserialize(using = EmptyStringAsNullDeserializer.class)
	private String title;
}
