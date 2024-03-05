package fr.rakambda.plexdeleter.api.servarr.sonarr.data;

import fr.rakambda.plexdeleter.api.servarr.data.Image;
import fr.rakambda.plexdeleter.api.servarr.data.OriginalLanguage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
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
	private boolean ended;
	@NotNull
	private Set<AlternativeTitle> alternateTitles = new HashSet<>();
	@NotNull
	private Set<Image> images = new HashSet<>();
	@Nullable
	private Instant previousAiring;
	@NotNull
	private OriginalLanguage originalLanguage;
	@NotNull
	private Set<Season> seasons = new HashSet<>();
	@NotNull
	private Set<String> genres = new HashSet<>();
	@NotNull
	private Set<Integer> tags = new HashSet<>();
	private int year;
	@NotNull
	private String path;
	@NotNull
	private String rootFolderPath;
	@Nullable
	private String certification;
	private int qualityProfileId;
	private int languageProfileId;
	private int id;
	private boolean seasonFolder;
	private boolean monitored;
	private boolean useSceneNumbering;
	@NotNull
	private String monitorNewItems;
	private int runtime;
	private int tvdbId;
	@Nullable
	private Integer tvRageId;
	@Nullable
	private Integer tvMazeId;
	@Nullable
	private String imdbId;
	@Nullable
	private Instant firstAired;
	@Nullable
	private Instant lastAired;
	@Nullable
	private Instant added;
	@NotNull
	private String seriesType;
	@NotNull
	private String cleanTitle;
	@NotNull
	private String titleSlug;
	@NotNull
	private Statistics statistics;
}
