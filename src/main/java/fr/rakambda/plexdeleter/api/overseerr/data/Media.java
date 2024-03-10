package fr.rakambda.plexdeleter.api.overseerr.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterReflectionForBinding(Media.class)
public sealed class Media permits MovieMedia, SeriesMedia{
	@Nullable
	private Set<Genre> genres;
	@Nullable
	private Set<RelatedVideo> relatedVideos;
	private String homepage;
	private int id;
	private String originalLanguage;
	private String tagline;
	private String overview;
	private Float popularity;
	@Nullable
	private Set<ProductionCompany> productionCompanies;
	@Nullable
	private Set<Country> productionCountries;
	@Nullable
	private Set<Language> spokenLanguages;
	private String status;
	private Float voteAverage;
	private Integer voteCount;
	private String backdropPath;
	private String posterPath;
	private Credits credits;
	private ExternalIds externalIds;
	@Nullable
	private MediaInfo mediaInfo;
	@Nullable
	private Set<Keyword> keywords;
	@Nullable
	private Set<WatchProvider> watchProviders;
}
