package fr.rakambda.plexdeleter.api.overseerr.data;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "mediaInfo.mediaType")
@JsonSubTypes(value = {
		@JsonSubTypes.Type(value = MovieMedia.class, name = "movie"),
		@JsonSubTypes.Type(value = SeriesMedia.class, name = "tv"),
})
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
	@NonNull
	private MediaInfo mediaInfo;
	private Set<Keyword> keywords = new HashSet<>();
	private Set<WatchProvider> watchProviders = new HashSet<>();
}
