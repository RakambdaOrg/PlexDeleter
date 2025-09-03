package fr.rakambda.plexdeleter.api.servarr.radarr.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterReflectionForBinding(AlternativeTitle.class)
public final class AlternativeTitle{
	@NonNull
	private String title;
	@NonNull
	private String sourceType;
	private int id;
	private int movieMetadataId;
}
