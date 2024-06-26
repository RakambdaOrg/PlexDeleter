package fr.rakambda.plexdeleter.notify.context;

import fr.rakambda.plexdeleter.api.tautulli.TautulliService;
import fr.rakambda.plexdeleter.api.tautulli.data.GetMetadataResponse;
import fr.rakambda.plexdeleter.api.tvdb.TvdbService;
import fr.rakambda.plexdeleter.api.tvdb.data.MediaData;
import fr.rakambda.plexdeleter.api.tvdb.data.Translation;
import fr.rakambda.plexdeleter.api.tvdb.data.TvdbResponseWrapper;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
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
public class TvdbMediaMetadataContext extends MediaMetadataContext{
	@NotNull
	private final TvdbService tvdbService;
	
	@NotNull
	private final Map<Integer, MediaData> mediaDatas = new HashMap<>();
	@NotNull
	private final Map<Integer, Map<Locale, Translation>> mediaTranslations = new HashMap<>();
	@NotNull
	private final Map<Integer, Map<Locale, Translation>> seasonTranslations = new HashMap<>();
	@NotNull
	private final Map<Integer, Map<Locale, Translation>> episodeTranslations = new HashMap<>();
	
	public TvdbMediaMetadataContext(@NotNull TautulliService tautulliService, @NotNull GetMetadataResponse metadata, @NotNull TvdbService tvdbService){
		super(tautulliService, metadata);
		this.tvdbService = tvdbService;
	}
	
	@NotNull
	public Optional<String> getTitle(@NotNull Locale locale){
		return getMediaTranslation(locale).map(Translation::getName);
	}
	
	@NotNull
	public Optional<String> getSummary(@NotNull Locale locale){
		return getElementTranslation(locale).map(Translation::getOverview);
	}
	
	@NotNull
	public Optional<Collection<String>> getGenres(@NotNull MessageSource messageSource, @NotNull Locale locale){
		return getMediaData()
				.map(MediaData::getGenres)
				.filter(g -> !g.isEmpty())
				.map(gs -> gs.stream()
						.map(g -> translateSlug(messageSource, "genre", g.getSlug(), locale)
								.orElseGet(() -> "%s (%s)".formatted(g.getName(), g.getSlug())))
						.toList());
	}
	
	@NotNull
	private Optional<String> translateSlug(@NotNull MessageSource messageSource, @NotNull String type, @NotNull String slug, @NotNull Locale locale){
		var key = "tvdb.%s.%s".formatted(type, slug);
		try{
			return Optional.of(messageSource.getMessage(key, new Object[0], locale));
		}
		catch(NoSuchMessageException e){
			log.warn("Could not find translation key {} with locale {}", key, locale);
			return Optional.empty();
		}
	}
	
	@NotNull
	public Optional<Integer> getTvdbId(){
		return getTvdbId(getMetadata().getGrandparentGuids())
				.or(() -> getTvdbId(getMetadata().getParentGuids()))
				.or(() -> getTvdbId(getMetadata().getGuids()));
	}
	
	@NotNull
	private Optional<Integer> getTvdbId(@NotNull Collection<String> guids){
		return guids.stream()
				.filter(guid -> guid.matches("tvdb://\\d+"))
				.map(guid -> guid.substring("tvdb://".length()))
				.map(Integer::parseInt)
				.findFirst();
	}
	
	@NotNull
	private Optional<? extends MediaData> getMediaData(){
		var tvdbId = getTvdbId().orElse(null);
		
		if(Objects.isNull(tvdbId)){
			return Optional.empty();
		}
		
		var tvdbCache = Optional.ofNullable(mediaDatas.get(tvdbId));
		if(tvdbCache.isPresent()){
			return tvdbCache;
		}
		
		try{
			var response = switch(getMetadata().getMediaType()){
				case MOVIE -> tvdbService.getExtendedMovieData(tvdbId);
				case SEASON, SHOW, EPISODE -> tvdbService.getExtendedSeriesData(tvdbId);
				case TRACK, ARTIST, PHOTO -> null;
			};
			
			var data = Optional.ofNullable(response).map(TvdbResponseWrapper::getData);
			data.ifPresent(t -> mediaDatas.put(tvdbId, t));
			return data;
		}
		catch(Exception e){
			log.warn("Failed to get media data with Tvdb id {}", tvdbId);
			return Optional.empty();
		}
	}
	
	@NotNull
	private Optional<Translation> getMediaTranslation(@NotNull Locale locale){
		var tvdbId = getTvdbId().orElse(null);
		
		if(Objects.isNull(tvdbId)){
			return Optional.empty();
		}
		
		var tvdbCache = Optional.ofNullable(mediaTranslations.get(tvdbId)).map(cache -> cache.get(locale));
		if(tvdbCache.isPresent()){
			return tvdbCache;
		}
		
		try{
			var response = switch(getMetadata().getMediaType()){
				case MOVIE -> tvdbService.getMovieTranslations(tvdbId, locale);
				case SEASON, SHOW, EPISODE -> tvdbService.getSeriesTranslations(tvdbId, locale);
				case TRACK, ARTIST, PHOTO -> null;
			};
			
			var translation = Optional.ofNullable(response).map(TvdbResponseWrapper::getData);
			translation.ifPresent(t -> mediaTranslations.computeIfAbsent(tvdbId, k -> new HashMap<>()).put(locale, t));
			return translation;
		}
		catch(Exception e){
			log.warn("Failed to get translations with Tvdb id {} and locale {}", tvdbId, locale);
			return Optional.empty();
		}
	}
	
	@NotNull
	private Optional<Translation> getElementTranslation(@NotNull Locale locale){
		return switch(getMetadata().getMediaType()){
			case MOVIE, SHOW -> getMediaTranslation(locale);
			case SEASON -> getSeasonTranslation(locale)
					.or(() -> getMediaTranslation(locale));
			case EPISODE -> getEpisodeTranslation(locale)
					.or(() -> getSeasonTranslation(locale))
					.or(() -> getMediaTranslation(locale));
			case TRACK, ARTIST, PHOTO -> Optional.empty();
		};
	}
	
	@NotNull
	private Optional<Translation> getSeasonTranslation(@NotNull Locale locale){
		var tvdbId = switch(getMetadata().getMediaType()){
			case SEASON, SHOW -> getTvdbId(getMetadata().getGuids()).orElse(null);
			case EPISODE -> getTvdbId(getMetadata().getParentGuids()).orElse(null);
			case MOVIE, TRACK, ARTIST, PHOTO -> null;
		};
		
		if(Objects.isNull(tvdbId)){
			return Optional.empty();
		}
		
		var tvdbCache = Optional.ofNullable(seasonTranslations.get(tvdbId)).map(cache -> cache.get(locale));
		if(tvdbCache.isPresent()){
			return tvdbCache;
		}
		
		try{
			var response = tvdbService.getSeasonTranslations(tvdbId, locale);
			var translation = Optional.of(response).map(TvdbResponseWrapper::getData);
			translation.ifPresent(t -> seasonTranslations.computeIfAbsent(tvdbId, k -> new HashMap<>()).put(locale, t));
			return translation;
		}
		catch(Exception e){
			log.warn("Failed to get season translations with Tvdb id {} and locale {}", tvdbId, locale);
			return Optional.empty();
		}
	}
	
	@NotNull
	private Optional<Translation> getEpisodeTranslation(@NotNull Locale locale){
		var tvdbId = switch(getMetadata().getMediaType()){
			case EPISODE -> getTvdbId(getMetadata().getGuids()).orElse(null);
			case TRACK, MOVIE, SEASON, SHOW, ARTIST, PHOTO -> null;
		};
		
		if(Objects.isNull(tvdbId)){
			return Optional.empty();
		}
		
		var tvdbCache = Optional.ofNullable(episodeTranslations.get(tvdbId)).map(cache -> cache.get(locale));
		if(tvdbCache.isPresent()){
			return tvdbCache;
		}
		
		try{
			var response = tvdbService.getEpisodeTranslations(tvdbId, locale);
			var translation = Optional.of(response).map(TvdbResponseWrapper::getData);
			translation.ifPresent(t -> episodeTranslations.computeIfAbsent(tvdbId, k -> new HashMap<>()).put(locale, t));
			return translation;
		}
		catch(Exception e){
			log.warn("Failed to get episode translations with Tvdb id {} and locale {}", tvdbId, locale);
			return Optional.empty();
		}
	}
	
	@Override
	public Collection<MetadataProviderInfo> getMetadataProviderInfo(){
		var type = switch(getMetadata().getMediaType()){
			case MOVIE -> "movie";
			case SHOW, SEASON, EPISODE -> "series";
			case TRACK, ARTIST, PHOTO -> null;
		};
		
		return getTvdbId()
				.map(id -> "https://www.thetvdb.com/dereferrer/%s/%s".formatted(type, id))
				.map(url -> new MetadataProviderInfo("Tvdb", url))
				.map(List::of)
				.orElseGet(List::of);
	}
}
