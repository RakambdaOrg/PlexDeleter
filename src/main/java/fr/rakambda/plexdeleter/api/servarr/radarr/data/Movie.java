package fr.rakambda.plexdeleter.api.servarr.radarr.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import fr.rakambda.plexdeleter.api.servarr.data.Image;
import fr.rakambda.plexdeleter.api.servarr.data.OriginalLanguage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import java.time.Instant;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterReflectionForBinding(Movie.class)
public final class Movie{
	@NotNull
	private String title;
	@NotNull
	private String originalTitle;
	@NotNull
	private OriginalLanguage originalLanguage;
	@Nullable
	private Set<AlternativeTitle> alternateTitles;
	private int secondaryYearSourceId;
	@NotNull
	private String sortTitle;
	private long sizeOnDisk;
	@NotNull
	private String status;
	@NotNull
	private String overview;
	@Nullable
	private Instant inCinemas;
	@Nullable
	private Instant physicalRelease;
	@Nullable
	private Instant digitalRelease;
	@Nullable
	private Set<Image> images;
	@Nullable
	private String website;
	@Nullable
	private String studio;
	@Nullable
	private String youTubeTrailerId;
	private int year;
	@NotNull
	private String path;
	@NotNull
	private String folderName;
	@NotNull
	private String minimumAvailability;
	private int qualityProfileId;
	private boolean monitored;
	private boolean hasFile;
	@JsonProperty("isAvailable")
	private boolean available;
	private int runtime;
	@NotNull
	private String cleanTitle;
	@NotNull
	private String titleSlug;
	@Nullable
	private String imdbId;
	@Nullable
	private Integer tmdbId;
	@NotNull
	private String rootFolderPath;
	@Nullable
	private String certification;
	@Nullable
	private Set<String> genres;
	@Nullable
	private Set<Integer> tags;
	@Nullable
	private Instant added;
	@Nullable
	private MovieFile movieFile;
	@Nullable
	private Collection collection;
	private float popularity;
	@NotNull
	private Statistics statistics;
	private int id;
}
