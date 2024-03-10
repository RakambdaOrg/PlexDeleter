package fr.rakambda.plexdeleter.api.overseerr.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterReflectionForBinding(WatchProvider.class)
public class WatchProvider{
	private String iso_3166_1;
	private String link;
	@Nullable
	private Set<Buy> buy;
	@Nullable
	private Set<Flatrate> flatrate;
}
