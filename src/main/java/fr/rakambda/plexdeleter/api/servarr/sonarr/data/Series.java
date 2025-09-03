package fr.rakambda.plexdeleter.api.servarr.sonarr.data;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import fr.rakambda.plexdeleter.api.servarr.data.Image;
import fr.rakambda.plexdeleter.api.servarr.data.OriginalLanguage;
import fr.rakambda.plexdeleter.json.InstantAsStringWithoutNanosSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterReflectionForBinding(Series.class)
public final class Series{
	@NonNull
	private String title;
	@NonNull
	private String sortTitle;
	@NonNull
	private String overview;
	@NonNull
	private String status;
	@Nullable
	private String network;
	@Nullable
	private String airTime;
	@NonNull
	private Boolean ended;
	@NonNull
	private Set<AlternativeTitle> alternateTitles = new HashSet<>();
	@NonNull
	private Set<Image> images = new HashSet<>();
	@Nullable
	@JsonSerialize(using = InstantAsStringWithoutNanosSerializer.class)
	private Instant previousAiring;
	@NonNull
	private OriginalLanguage originalLanguage;
	@NonNull
	private Set<Season> seasons = new HashSet<>();
	@NonNull
	private Set<String> genres = new HashSet<>();
	@NonNull
	private Set<Integer> tags = new HashSet<>();
	@NonNull
	private Integer year;
	@NonNull
	private String path;
	@NonNull
	private String rootFolderPath;
	@Nullable
	private String certification;
	@NonNull
	private Integer qualityProfileId;
	@NonNull
	private Integer languageProfileId;
	@NonNull
	private Integer id;
	@NonNull
	private Boolean seasonFolder;
	@NonNull
	private Boolean monitored;
	@NonNull
	private Boolean useSceneNumbering;
	@NonNull
	private String monitorNewItems;
	@NonNull
	private Integer runtime;
	@Nullable
	private Integer tvdbId;
	@Nullable
	private Integer tvRageId;
	@Nullable
	private Integer tvMazeId;
	@Nullable
	private String imdbId;
	@Nullable
	@JsonSerialize(using = InstantAsStringWithoutNanosSerializer.class)
	private Instant firstAired;
	@Nullable
	@JsonSerialize(using = InstantAsStringWithoutNanosSerializer.class)
	private Instant lastAired;
	@Nullable
	@JsonSerialize(using = InstantAsStringWithoutNanosSerializer.class)
	private Instant added;
	@NonNull
	private String seriesType;
	@NonNull
	private String cleanTitle;
	@Nullable
	private String titleSlug;
	@NonNull
	private Statistics statistics;
}
