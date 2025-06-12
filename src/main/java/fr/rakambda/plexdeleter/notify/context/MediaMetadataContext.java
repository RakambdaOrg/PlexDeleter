package fr.rakambda.plexdeleter.notify.context;

import fr.rakambda.plexdeleter.api.tautulli.TautulliApiService;
import fr.rakambda.plexdeleter.api.tautulli.data.GetMetadataResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.context.MessageSource;
import java.util.Collection;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;

@Slf4j
@RequiredArgsConstructor
public abstract class MediaMetadataContext{
	@NotNull
	private final TautulliApiService tautulliApiService;
	@NotNull
	@Getter
	private final GetMetadataResponse metadata;
	
	private boolean posterCalled = false;
	@Nullable
	private byte[] posterData = null;
	
	@NotNull
	public abstract Optional<String> getTitle(@NotNull Locale locale);
	
	@NotNull
	public abstract Optional<String> getSummary(@NotNull Locale locale);
	
	@NotNull
	public abstract Optional<Collection<String>> getGenres(@NotNull MessageSource messageSource, @NotNull Locale locale);
	
	@NotNull
	public Optional<byte[]> getPosterData(){
		if(posterCalled){
			return Optional.ofNullable(posterData);
		}
		
		posterCalled = true;
		Function<Integer, Optional<byte[]>> posterFunction = ratingKey -> tautulliApiService.getPosterBytes(ratingKey, 222, 333)
				.filter(d -> d.length > 0);
		
		try{
			var data = switch(metadata.getMediaType()){
				case EPISODE -> posterFunction.apply(metadata.getParentRatingKey()).or(() -> posterFunction.apply(metadata.getGrandparentRatingKey()));
				case SEASON, TRACK, PHOTO -> posterFunction.apply(metadata.getRatingKey()).or(() -> posterFunction.apply(metadata.getParentRatingKey()));
				case MOVIE, ARTIST, SHOW -> posterFunction.apply(metadata.getRatingKey());
			};
			
			data.ifPresent(bytes -> posterData = bytes);
			return data;
		}
		catch(Exception e){
			log.warn("Failed to get poster data for {}", metadata, e);
			return Optional.empty();
		}
	}
	
	public abstract Collection<MetadataProviderInfo> getMetadataProviderInfo();
}
