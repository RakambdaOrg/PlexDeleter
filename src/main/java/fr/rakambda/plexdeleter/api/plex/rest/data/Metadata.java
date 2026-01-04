package fr.rakambda.plexdeleter.api.plex.rest.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterReflectionForBinding(Metadata.class)
public class Metadata{
	@Nullable
	@JsonProperty("Collection")
	private List<Collection> collection;
}
