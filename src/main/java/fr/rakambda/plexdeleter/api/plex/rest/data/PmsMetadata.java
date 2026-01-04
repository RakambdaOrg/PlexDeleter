package fr.rakambda.plexdeleter.api.plex.rest.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterReflectionForBinding(PmsMetadata.class)
public class PmsMetadata{
	@NonNull
	@JsonProperty("MediaContainer")
	private MediaContainer mediaContainer;
}
