package fr.rakambda.plexdeleter.notify.context;

import fr.rakambda.plexdeleter.api.RequestFailedException;
import fr.rakambda.plexdeleter.api.servarr.data.Tag;
import fr.rakambda.plexdeleter.api.servarr.radarr.RadarrApiService;
import fr.rakambda.plexdeleter.api.servarr.sonarr.SonarrApiService;
import fr.rakambda.plexdeleter.api.tautulli.data.GetMetadataResponse;
import fr.rakambda.plexdeleter.storage.entity.MediaEntity;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.context.MessageSource;
import org.springframework.web.client.RestClientException;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class ServarrMediaMetadataContext extends MediaMetadataContext{
	
	@NonNull
	private final RadarrApiService radarrApiService;
	@NonNull
	private final SonarrApiService sonarrApiService;
	
	public ServarrMediaMetadataContext(@NonNull GetMetadataResponse metadata, @NonNull RadarrApiService radarrApiService, @NonNull SonarrApiService sonarrApiService){
		super(metadata);
		this.radarrApiService = radarrApiService;
		this.sonarrApiService = sonarrApiService;
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
		if(Objects.isNull(media) || Objects.isNull(media.getServarrId())){
			return Optional.empty();
		}
		try{
			var appliedTags = switch(media.getType()){
				case SEASON -> sonarrApiService.getSeries(media.getServarrId()).getTags();
				case MOVIE -> radarrApiService.getMovie(media.getServarrId()).getTags();
			};
			if(appliedTags.isEmpty()){
				return Optional.empty();
			}
			
			var tags = (switch(media.getType()){
				case SEASON -> sonarrApiService.getTags();
				case MOVIE -> radarrApiService.getTags();
			}).stream().collect(Collectors.toMap(Tag::getId, Tag::getLabel));
			
			return Optional.of(appliedTags.stream()
					.map(id -> tags.getOrDefault(id, "unknown"))
					.toList());
		}
		catch(RequestFailedException | RestClientException e){
			log.error("Failed to get Servarr tags", e);
			return Optional.empty();
		}
	}
	
	@Override
	@NonNull
	public Optional<byte[]> getPosterData(){
		return Optional.empty();
	}
	
	@Override
	public Collection<MetadataProviderInfo> getMetadataProviderInfo(){
		return List.of();
	}
}
