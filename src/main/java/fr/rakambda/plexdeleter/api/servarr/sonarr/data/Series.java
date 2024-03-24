package fr.rakambda.plexdeleter.api.servarr.sonarr.data;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import fr.rakambda.plexdeleter.api.servarr.data.Image;
import fr.rakambda.plexdeleter.api.servarr.data.OriginalLanguage;
import fr.rakambda.plexdeleter.json.InstantAsStringWithoutNanosSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterReflectionForBinding(Series.class)
public final class Series{
	@NotNull
	private String title;
	@NotNull
	private String sortTitle;
	@NotNull
	private String overview;
	@NotNull
	private String status;
	@Nullable
	private String network;
	@Nullable
	private String airTime;
	@NotNull
	private Boolean ended;
	@NotNull
	private Set<AlternativeTitle> alternateTitles = new HashSet<>();
	@NotNull
	private Set<Image> images = new HashSet<>();
	@Nullable
	@JsonSerialize(using = InstantAsStringWithoutNanosSerializer.class)
	private Instant previousAiring;
	@NotNull
	private OriginalLanguage originalLanguage;
	@NotNull
	private Set<Season> seasons = new HashSet<>();
	@NotNull
	private Set<String> genres = new HashSet<>();
	@NotNull
	private Set<Integer> tags = new HashSet<>();
	@NotNull
	private Integer year;
	@NotNull
	private String path;
	@NotNull
	private String rootFolderPath;
	@Nullable
	private String certification;
	@NotNull
	private Integer qualityProfileId;
	@NotNull
	private Integer languageProfileId;
	@NotNull
	private Integer id;
	@NotNull
	private Boolean seasonFolder;
	@NotNull
	private Boolean monitored;
	@NotNull
	private Boolean useSceneNumbering;
	@NotNull
	private String monitorNewItems;
	@NotNull
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
	@NotNull
	private String seriesType;
	@NotNull
	private String cleanTitle;
	@Nullable
	private String titleSlug;
	@NotNull
	private Statistics statistics;
}
