package fr.rakambda.plexdeleter.api.servarr.radarr.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterReflectionForBinding(Collection.class)
public final class Collection{
	@NotNull
	private String title;
	private int tmdbId;
}
