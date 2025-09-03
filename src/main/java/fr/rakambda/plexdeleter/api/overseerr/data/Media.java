package fr.rakambda.plexdeleter.api.overseerr.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterReflectionForBinding(Media.class)
public sealed class Media permits MovieMedia, SeriesMedia{
	private Set<Genre> genres = new HashSet<>();
	private Set<RelatedVideo> relatedVideos = new HashSet<>();
	private String homepage;
	private int id;
	private String originalLanguage;
	private String tagline;
	private String overview;
	private Float popularity;
	private Set<ProductionCompany> productionCompanies = new HashSet<>();
	private Set<Country> productionCountries = new HashSet<>();
	private Set<Language> spokenLanguages = new HashSet<>();
	private String status;
	private Float voteAverage;
	private Integer voteCount;
	private String backdropPath;
	private String posterPath;
	private Credits credits;
	private ExternalIds externalIds;
	@Nullable
	private MediaInfo mediaInfo;
	private Set<Keyword> keywords = new HashSet<>();
	private Set<WatchProvider> watchProviders = new HashSet<>();
}
