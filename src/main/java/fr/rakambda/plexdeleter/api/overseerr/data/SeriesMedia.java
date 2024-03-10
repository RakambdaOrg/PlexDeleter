package fr.rakambda.plexdeleter.api.overseerr.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@RegisterReflectionForBinding(SeriesMedia.class)
public final class SeriesMedia extends Media{
	private Set<CreatedBy> createdBy = new HashSet<>();
	private Set<Integer> episodeRunTime = new HashSet<>();
	private LocalDate firstAirDate;
	private LocalDate lastAirDate;
	private boolean inProduction;
	private Set<String> languages = new HashSet<>();
	private String name;
	private Set<Network> networks = new HashSet<>();
	private int numberOfEpisodes;
	private int numberOfSeasons;
	private Set<String> originCountry = new HashSet<>();
	private String originalName;
	private ContentRatings contentRatings;
	private Set<Season> seasons = new HashSet<>();
	private String type;
	private Episode lastEpisodeToAir;
}
