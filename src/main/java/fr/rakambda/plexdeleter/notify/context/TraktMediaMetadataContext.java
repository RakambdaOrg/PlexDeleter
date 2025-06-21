package fr.rakambda.plexdeleter.notify.context;

import fr.rakambda.plexdeleter.api.tautulli.TautulliApiService;
import fr.rakambda.plexdeleter.api.tautulli.data.GetMetadataResponse;
import fr.rakambda.plexdeleter.api.tmdb.data.MediaData;
import fr.rakambda.plexdeleter.api.tmdb.data.RootMediaData;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.MessageSource;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class TraktMediaMetadataContext extends MediaMetadataContext{
	@NotNull
	private final Map<Integer, Map<Locale, RootMediaData>> mediaTranslations = new HashMap<>();
	@NotNull
	private final Map<Integer, Map<Locale, MediaData>> seasonTranslations = new HashMap<>();
	
	public TraktMediaMetadataContext(@NotNull TautulliApiService tautulliApiService, @NotNull GetMetadataResponse metadata){
		super(tautulliApiService, metadata);
	}
	
	@NotNull
	public Optional<String> getTitle(@NotNull Locale locale){
		return Optional.empty();
	}
	
	@NotNull
	public Optional<String> getSummary(@NotNull Locale locale){
		return Optional.empty();
	}
	
	@NotNull
	public Optional<Collection<String>> getGenres(@NotNull MessageSource messageSource, @NotNull Locale locale){
		return Optional.empty();
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
	
	@Override
	public Collection<MetadataProviderInfo> getMetadataProviderInfo(){
		var type = switch(getMetadata().getMediaType()){
			case MOVIE -> "movie";
			case SHOW, SEASON, EPISODE -> "show";
			case TRACK, ARTIST, PHOTO -> null;
		};
		
		return getTmdbId()
				.map(id -> "https://trakt.tv/search/tmdb/%d?id_type=%s".formatted(id, type))
				.map(url -> new MetadataProviderInfo("Trakt", url))
				.map(List::of)
				.orElseGet(List::of);
	}
}
