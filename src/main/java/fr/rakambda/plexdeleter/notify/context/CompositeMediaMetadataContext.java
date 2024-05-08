package fr.rakambda.plexdeleter.notify.context;

import fr.rakambda.plexdeleter.api.tautulli.TautulliService;
import fr.rakambda.plexdeleter.api.tautulli.data.GetMetadataResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.MessageSource;
import java.util.Collection;
import java.util.Locale;
import java.util.Optional;

public class CompositeMediaMetadataContext extends MediaMetadataContext{
	@NotNull
	private final Collection<MediaMetadataContext> contexts;
	
	public CompositeMediaMetadataContext(@NotNull TautulliService tautulliService, @NotNull GetMetadataResponse metadata, @NotNull Collection<MediaMetadataContext> contexts){
		super(tautulliService, metadata);
		this.contexts = contexts;
	}
	
	@NotNull
	public Optional<String> getTitle(@NotNull Locale locale){
		return contexts.stream()
				.map(c -> c.getTitle(locale))
				.flatMap(Optional::stream)
				.findFirst();
	}
	
	@NotNull
	public Optional<String> getSummary(@NotNull Locale locale){
		return contexts.stream()
				.map(c -> c.getSummary(locale))
				.flatMap(Optional::stream)
				.findFirst();
	}
	
	@NotNull
	public Optional<Collection<String>> getGenres(@NotNull MessageSource messageSource, @NotNull Locale locale){
		return contexts.stream()
				.map(c -> c.getGenres(messageSource, locale))
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
}
