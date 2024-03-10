package fr.rakambda.plexdeleter.api.overseerr.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.Nullable;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import java.time.LocalDate;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@RegisterReflectionForBinding(SeriesMedia.class)
public final class SeriesMedia extends Media{
	@Nullable
	private Set<CreatedBy> createdBy;
	@Nullable
	private Set<Integer> episodeRunTime;
	private LocalDate firstAirDate;
	private LocalDate lastAirDate;
	private boolean inProduction;
	@Nullable
	private Set<String> languages;
	private String name;
	@Nullable
	private Set<Network> networks;
	private int numberOfEpisodes;
	private int numberOfSeasons;
	@Nullable
	private Set<String> originCountry;
	private String originalName;
	private ContentRatings contentRatings;
	@Nullable
	private Set<Season> seasons;
	private String type;
	private Episode lastEpisodeToAir;
}
