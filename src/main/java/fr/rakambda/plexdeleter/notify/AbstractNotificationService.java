package fr.rakambda.plexdeleter.notify;

import fr.rakambda.plexdeleter.api.RequestFailedException;
import fr.rakambda.plexdeleter.api.tautulli.data.GetMetadataResponse;
import fr.rakambda.plexdeleter.api.tautulli.data.MediaInfo;
import fr.rakambda.plexdeleter.api.tautulli.data.MediaPart;
import fr.rakambda.plexdeleter.service.WatchService;
import fr.rakambda.plexdeleter.storage.entity.MediaEntity;
import fr.rakambda.plexdeleter.storage.entity.UserGroupEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public abstract class AbstractNotificationService{
	protected static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	
	private final WatchService watchService;
	private final Map<String, Locale> languages;
	
	protected AbstractNotificationService(WatchService watchService){
		this.watchService = watchService;
		
		this.languages = new HashMap<>();
		for(var locale : Locale.getAvailableLocales()){
			this.languages.put(locale.getISO3Language(), locale);
		}
	}
	
	@NotNull
	protected <T> Stream<T> getMediaStreams(@NotNull GetMetadataResponse metadata, @NotNull Class<T> klass){
		return metadata.getMediaInfo().stream()
				.map(MediaInfo::getParts)
				.flatMap(Collection::stream)
				.map(MediaPart::getStreams)
				.flatMap(Collection::stream)
				.filter(klass::isInstance)
				.map(klass::cast);
	}
	
	@NotNull
	protected Stream<String> getLanguageName(@Nullable String code, @NotNull Locale locale){
		if(Objects.isNull(code)){
			return Stream.empty();
		}
		return Stream.of(Optional.ofNullable(languages.get(code))
				.map(l -> l.getDisplayLanguage(locale))
				.orElse(code));
	}
	
	@NotNull
	public Collection<String> getEpisodes(@NotNull MediaEntity media, @NotNull UserGroupEntity userGroupEntity) throws RequestFailedException{
		return switch(media.getType()){
			case MOVIE -> List.of();
			case SEASON -> Objects.isNull(media.getPlexId()) ? List.of() : watchService.getGroupWatchHistory(userGroupEntity, media).entrySet().stream()
					.filter(entry -> entry.getValue().stream().allMatch(r -> Objects.equals(r.getWatchedStatus(), 0)))
					.map(Map.Entry::getKey)
					.map(String::valueOf)
					.toList();
		};
	}
	
	@NotNull
	public String getTypeKey(@NotNull MediaEntity media){
		return switch(media.getType()){
			case MOVIE -> "mail.watchlist.body.media.movie";
			case SEASON -> "mail.watchlist.body.media.series";
		};
	}
}
