package fr.rakambda.plexdeleter.api.servarr.sonarr.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterReflectionForBinding(AlternativeTitle.class)
public final class AlternativeTitle{
	@NonNull
	private String title;
	@Nullable
	private Integer seasonNumber;
}
