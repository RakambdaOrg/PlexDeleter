package fr.rakambda.plexdeleter.api.overseerr.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterReflectionForBinding({WatchProvider.class})
public class WatchProvider{
	private String iso_3166_1;
	private String link;
	private Set<Buy> buy = new HashSet<>();
	private Set<Flatrate> flatrate = new HashSet<>();
}
