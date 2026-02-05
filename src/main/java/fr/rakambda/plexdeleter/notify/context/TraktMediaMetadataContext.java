package fr.rakambda.plexdeleter.notify.context;

import fr.rakambda.plexdeleter.api.tautulli.data.GetMetadataResponse;
import fr.rakambda.plexdeleter.storage.entity.MediaEntity;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.context.MessageSource;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Slf4j
public class TraktMediaMetadataContext extends MediaMetadataContext{
	public TraktMediaMetadataContext(@NonNull GetMetadataResponse metadata){
		super(metadata);
	}
	
	@NonNull
	public Optional<String> getTitle(@NonNull Locale locale){
		return Optional.empty();
	}
	
	@NonNull
	public Optional<String> getSummary(@NonNull Locale locale){
		return Optional.empty();
	}
	
	@NonNull
	public Optional<Collection<String>> getGenres(@NonNull MessageSource messageSource, @NonNull Locale locale){
		return Optional.empty();
	}
	
	@Override
	@NonNull
	public Optional<Collection<String>> getServerTags(@Nullable MediaEntity media){
		return Optional.empty();
	}
	
	@Override
	@NonNull
	public Optional<byte[]> getPosterData(){
		return Optional.empty();
	}
	
	@NonNull
	public Optional<Integer> getTmdbId(){
		return getTmdbId(getMetadata().getGrandparentGuids())
				.or(() -> getTmdbId(getMetadata().getParentGuids()))
				.or(() -> getTmdbId(getMetadata().getGuids()));
	}
	
	@NonNull
	private Optional<Integer> getTmdbId(@NonNull Collection<String> guids){
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
