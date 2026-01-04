package fr.rakambda.plexdeleter.api.plex.rest.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterReflectionForBinding(MediaContainer.class)
public class MediaContainer{
	@NonNull
	@JsonProperty("Metadata")
	private List<Metadata> metadata;
}
