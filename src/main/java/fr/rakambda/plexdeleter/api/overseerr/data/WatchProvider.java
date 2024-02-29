package fr.rakambda.plexdeleter.api.overseerr.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WatchProvider{
	private String iso_3166_1;
	private String link;
	private Set<Buy> buy = new HashSet<>();
	private Set<Flatrate> flatrate = new HashSet<>();
}
