package fr.rakambda.plexdeleter.notify.context;

import fr.rakambda.plexdeleter.api.tautulli.TautulliService;
import fr.rakambda.plexdeleter.api.tautulli.data.GetMetadataResponse;
import fr.rakambda.plexdeleter.api.tmdb.TmdbService;
import fr.rakambda.plexdeleter.api.tmdb.data.MediaData;
import fr.rakambda.plexdeleter.api.tmdb.data.MovieData;
import fr.rakambda.plexdeleter.api.tmdb.data.RootMediaData;
import fr.rakambda.plexdeleter.api.tmdb.data.SeasonData;
import fr.rakambda.plexdeleter.api.tmdb.data.SeriesData;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Slf4j
public class TmdbMediaMetadataContext extends MediaMetadataContext{
	@NotNull
	private final TmdbService tmdbService;
	
	@NotNull
	private final Map<Integer, Map<Locale, RootMediaData>> mediaTranslations = new HashMap<>();
	@NotNull
	private final Map<Integer, Map<Locale, MediaData>> seasonTranslations = new HashMap<>();
	
	public TmdbMediaMetadataContext(@NotNull TautulliService tautulliService, @NotNull GetMetadataResponse metadata, @NotNull TmdbService tmdbService){
		super(tautulliService, metadata);
		this.tmdbService = tmdbService;
	}
	
	@NotNull
	public Optional<String> getTitle(@NotNull Locale locale){
		return getMediaTranslation(locale).map(m -> switch(m){
			case MovieData movieData -> Optional.ofNullable(movieData.getTitle()).orElseGet(movieData::getOriginalTitle);
			case SeriesData seriesData -> Optional.ofNullable(seriesData.getName()).orElseGet(seriesData::getOriginalName);
		});
	}
	
	@NotNull
	public Optional<String> getSummary(@NotNull Locale locale){
		return getElementTranslation(locale).map(MediaData::getOverview);
	}
	
	@NotNull
	public Optional<Collection<String>> getGenres(@NotNull MessageSource messageSource, @NotNull Locale locale){
		return getMediaTranslation(locale)
				.map(RootMediaData::getGenres)
				.filter(g -> !g.isEmpty())
				.map(gs -> gs.stream()
						.map(g -> translateId(messageSource, "genre", g.getId(), locale)
								.orElseGet(() -> "%s (%d)".formatted(g.getName(), g.getId())))
						.toList());
	}
	
	@NotNull
	private Optional<String> translateId(@NotNull MessageSource messageSource, @NotNull String type, int id, @NotNull Locale locale){
		var key = "tmdb.%s.%d".formatted(type, id);
		try{
			return Optional.of(messageSource.getMessage(key, new Object[0], locale));
		}
		catch(NoSuchMessageException e){
			log.warn("Could not find translation key {} with locale {}", key, locale);
			return Optional.empty();
		}
	}
	
	@NotNull
	public Optional<Integer> getTmdbId(){
		return getTmdbId(getMetadata().getGrandparentGuids())
				.or(() -> getTmdbId(getMetadata().getParentGuids()))
				.or(() -> getTmdbId(getMetadata().getGuids()));
	}
	
	@NotNull
	private Optional<Integer> getTmdbId(@NotNull Collection<String> guids){
		return guids.stream()
				.filter(guid -> guid.matches("tmdb://\\d+"))
				.map(guid -> guid.substring("tmdb://".length()))
				.map(Integer::parseInt)
				.findFirst();
	}
	
	@NotNull
	private Optional<RootMediaData> getMediaTranslation(@NotNull Locale locale){
		var tmdbId = getTmdbId().orElse(null);
		
		if(Objects.isNull(tmdbId)){
			return Optional.empty();
		}
		
		var tvdbCache = Optional.ofNullable(mediaTranslations.get(tmdbId)).map(cache -> cache.get(locale));
		if(tvdbCache.isPresent()){
			return tvdbCache;
		}
		
		try{
			var response = switch(getMetadata().getMediaType()){
				case MOVIE -> tmdbService.getMovieData(tmdbId, locale);
				case SHOW, SEASON, EPISODE -> tmdbService.getSeriesData(tmdbId, locale);
				case TRACK, ARTIST -> null;
			};
			
			mediaTranslations.computeIfAbsent(tmdbId, k -> new HashMap<>()).put(locale, response);
			return Optional.ofNullable(response);
		}
		catch(Exception e){
			log.warn("Failed to get translations with Tmdb id {} and locale {}", tmdbId, locale);
			return Optional.empty();
		}
	}
	
	@NotNull
	private Optional<? extends MediaData> getElementTranslation(@NotNull Locale locale){
		return switch(getMetadata().getMediaType()){
			case MOVIE, SHOW -> getMediaTranslation(locale);
			case SEASON -> getSeasonTranslation(locale, getMetadata().getMediaIndex())
					.or(() -> getMediaTranslation(locale));
			case EPISODE -> getSeasonTranslation(locale, getMetadata().getParentMediaIndex())
					.<MediaData> flatMap(data -> switch(data){
						case SeasonData series -> series.getEpisodes().stream()
								.filter(e -> Objects.equals(e.getEpisodeNumber(), getMetadata().getMediaIndex()))
								.findFirst();
						default -> Optional.empty();
					})
					.or(() -> getSeasonTranslation(locale, getMetadata().getParentMediaIndex()))
					.or(() -> getMediaTranslation(locale));
			case TRACK, ARTIST -> Optional.empty();
		};
	}
	
	@NotNull
	private Optional<MediaData> getSeasonTranslation(@NotNull Locale locale, @Nullable Integer seasonNumber){
		var tmdbId = getTmdbId().orElse(null);
		
		if(Objects.isNull(tmdbId) || Objects.isNull(seasonNumber)){
			return Optional.empty();
		}
		
		var tvdbCache = Optional.ofNullable(seasonTranslations.get(tmdbId)).map(cache -> cache.get(locale));
		if(tvdbCache.isPresent()){
			return tvdbCache;
		}
		
		try{
			var response = tmdbService.getSeasonData(tmdbId, seasonNumber, locale);
			seasonTranslations.computeIfAbsent(tmdbId, k -> new HashMap<>()).put(locale, response);
			return Optional.of(response);
		}
		catch(Exception e){
			log.warn("Failed to get season {} translations with Tmdb id {} and locale {}", seasonNumber, tmdbId, locale);
			return Optional.empty();
		}
	}
	
	@Override
	public Collection<MetadataProviderInfo> getMetadataProviderInfo(){
		var type = switch(getMetadata().getMediaType()){
			case MOVIE -> "movie";
			case SHOW, SEASON, EPISODE -> "tv";
			case TRACK, ARTIST -> null;
		};
		
		return getTmdbId()
				.map(id -> "https://www.themoviedb.org/%s/%d".formatted(type, id))
				.map(url -> new MetadataProviderInfo("Tmdb", url))
				.map(List::of)
				.orElseGet(List::of);
	}
}
