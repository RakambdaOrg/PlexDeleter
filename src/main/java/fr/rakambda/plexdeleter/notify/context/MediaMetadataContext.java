package fr.rakambda.plexdeleter.notify.context;

import fr.rakambda.plexdeleter.api.tautulli.data.GetMetadataResponse;
import fr.rakambda.plexdeleter.storage.entity.MediaEntity;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.context.MessageSource;
import java.util.Collection;
import java.util.Locale;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public abstract class MediaMetadataContext{
	@NonNull
	@Getter
	private final GetMetadataResponse metadata;
	
	@NonNull
	public abstract Optional<String> getTitle(@NonNull Locale locale);
	
	@NonNull
	public abstract Optional<String> getSummary(@NonNull Locale locale);
	
	@NonNull
	public abstract Optional<Collection<String>> getGenres(@NonNull MessageSource messageSource, @NonNull Locale locale);
	
	@NonNull
	public abstract Optional<Collection<String>> getServerTags(@Nullable MediaEntity media);
	
	@NonNull
	public abstract Optional<byte[]> getPosterData();
	
	public abstract Collection<MetadataProviderInfo> getMetadataProviderInfo();
}
