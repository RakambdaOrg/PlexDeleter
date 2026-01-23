package fr.rakambda.plexdeleter.api.servarr.radarr.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import fr.rakambda.plexdeleter.api.servarr.data.Image;
import fr.rakambda.plexdeleter.api.servarr.data.OriginalLanguage;
import fr.rakambda.plexdeleter.json.InstantAsStringWithoutNanosSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import tools.jackson.databind.annotation.JsonSerialize;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterReflectionForBinding(Movie.class)
public final class Movie{
	@NonNull
	private String title;
	@NonNull
	private String originalTitle;
	@NonNull
	private OriginalLanguage originalLanguage;
	@NonNull
	private Set<AlternativeTitle> alternateTitles = new HashSet<>();
	private int secondaryYearSourceId;
	@NonNull
	private String sortTitle;
	private long sizeOnDisk;
	@NonNull
	private String status;
	@NonNull
	private String overview;
	@Nullable
	@JsonSerialize(using = InstantAsStringWithoutNanosSerializer.class)
	private Instant inCinemas;
	@Nullable
	@JsonSerialize(using = InstantAsStringWithoutNanosSerializer.class)
	private Instant physicalRelease;
	@Nullable
	@JsonSerialize(using = InstantAsStringWithoutNanosSerializer.class)
	private Instant digitalRelease;
	@NonNull
	private Set<Image> images = new HashSet<>();
	@Nullable
	private String website;
	@Nullable
	private String studio;
	@Nullable
	private String youTubeTrailerId;
	private int year;
	@NonNull
	private String path;
	@NonNull
	private String folderName;
	@NonNull
	private String minimumAvailability;
	private int qualityProfileId;
	private boolean monitored;
	private boolean hasFile;
	@JsonProperty("isAvailable")
	private boolean available;
	private int runtime;
	@NonNull
	private String cleanTitle;
	@Nullable
	private String titleSlug;
	@Nullable
	private String imdbId;
	@Nullable
	private Integer tmdbId;
	@NonNull
	private String rootFolderPath;
	@Nullable
	private String certification;
	@NonNull
	private Set<String> genres = new HashSet<>();
	@NonNull
	private Set<Integer> tags = new HashSet<>();
	@Nullable
	@JsonSerialize(using = InstantAsStringWithoutNanosSerializer.class)
	private Instant added;
	@Nullable
	private MovieFile movieFile;
	@Nullable
	private Collection collection;
	private float popularity;
	@NonNull
	private Statistics statistics;
	private int id;
}
