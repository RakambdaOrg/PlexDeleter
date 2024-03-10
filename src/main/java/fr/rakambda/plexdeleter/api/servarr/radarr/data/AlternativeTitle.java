package fr.rakambda.plexdeleter.api.servarr.radarr.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterReflectionForBinding({AlternativeTitle.class})
public final class AlternativeTitle{
	@NotNull
	private String title;
	@NotNull
	private String sourceType;
	private int id;
	private int movieMetadataId;
}
