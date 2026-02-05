package fr.rakambda.plexdeleter.notify.context;

import fr.rakambda.plexdeleter.api.tautulli.TautulliApiService;
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
import java.util.function.Function;

@Slf4j
public class TautulliMediaMetadataContext extends MediaMetadataContext{
	
	@NonNull
	private final TautulliApiService tautulliApiService;
	
	@Nullable
	private byte[] posterData = null;
	private boolean posterCalled = false;
	
	public TautulliMediaMetadataContext(@NonNull GetMetadataResponse metadata, @NonNull TautulliApiService tautulliApiService){
		super(metadata);
		this.tautulliApiService = tautulliApiService;
	}
	
	@NonNull
	public Optional<byte[]> getPosterData(){
		if(posterCalled){
			return Optional.ofNullable(posterData);
		}
		
		posterCalled = true;
		Function<Integer, Optional<byte[]>> posterFunction = ratingKey -> tautulliApiService.getPosterBytes(ratingKey, 222, 333)
				.filter(d -> d.length > 0);
		
		try{
			var data = switch(getMetadata().getMediaType()){
				case EPISODE -> posterFunction.apply(getMetadata().getParentRatingKey()).or(() -> posterFunction.apply(getMetadata().getGrandparentRatingKey()));
				case SEASON, TRACK, PHOTO -> posterFunction.apply(getMetadata().getRatingKey()).or(() -> posterFunction.apply(getMetadata().getParentRatingKey()));
				case MOVIE, ARTIST, SHOW -> posterFunction.apply(getMetadata().getRatingKey());
			};
			
			data.ifPresent(bytes -> posterData = bytes);
			return data;
		}
		catch(Exception e){
			log.warn("Failed to get poster data for {}", getMetadata(), e);
			return Optional.empty();
		}
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
	public Collection<MetadataProviderInfo> getMetadataProviderInfo(){
		return List.of();
	}
}
