package fr.rakambda.plexdeleter.notify.context;

import fr.rakambda.plexdeleter.api.tautulli.data.GetMetadataResponse;
import fr.rakambda.plexdeleter.storage.entity.MediaEntity;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.context.MessageSource;
import java.util.Collection;
import java.util.Locale;
import java.util.Optional;

public class CompositeMediaMetadataContext extends MediaMetadataContext{
	@NonNull
	private final Collection<MediaMetadataContext> contexts;
	
	public CompositeMediaMetadataContext(@NonNull GetMetadataResponse metadata, @NonNull Collection<MediaMetadataContext> contexts){
		super(metadata);
		this.contexts = contexts;
	}
	
	@NonNull
	public Optional<String> getTitle(@NonNull Locale locale){
		return contexts.stream()
				.map(c -> c.getTitle(locale))
				.flatMap(Optional::stream)
				.findFirst();
	}
	
	@NonNull
	public Optional<String> getSummary(@NonNull Locale locale){
		return contexts.stream()
				.map(c -> c.getSummary(locale))
				.flatMap(Optional::stream)
				.findFirst();
	}
	
	@NonNull
	public Optional<Collection<String>> getGenres(@NonNull MessageSource messageSource, @NonNull Locale locale){
		return contexts.stream()
				.map(c -> c.getGenres(messageSource, locale))
				.flatMap(Optional::stream)
				.findFirst();
	}
	
	@Override
	@NonNull
	public Optional<Collection<String>> getServerTags(@Nullable MediaEntity media){
		return contexts.stream()
				.map(c -> c.getServerTags(media))
				.flatMap(Optional::stream)
				.findFirst();
	}
	
	@Override
	@NonNull
	public Optional<byte[]> getPosterData(){
		return contexts.stream()
				.map(MediaMetadataContext::getPosterData)
				.flatMap(Optional::stream)
				.findFirst();
	}
	
	@Override
	public Collection<MetadataProviderInfo> getMetadataProviderInfo(){
		return contexts.stream()
				.map(MediaMetadataContext::getMetadataProviderInfo)
				.flatMap(Collection::stream)
				.toList();
	}
	
	@NonNull
	public <T> Optional<T> find(Class<T> clazz){
		return contexts.stream()
				.filter(clazz::isInstance)
				.map(clazz::cast)
				.findFirst();
	}
}
