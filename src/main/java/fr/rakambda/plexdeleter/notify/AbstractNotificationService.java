package fr.rakambda.plexdeleter.notify;

import fr.rakambda.plexdeleter.api.RequestFailedException;
import fr.rakambda.plexdeleter.api.tautulli.TautulliService;
import fr.rakambda.plexdeleter.api.tautulli.data.GetMetadataResponse;
import fr.rakambda.plexdeleter.api.tautulli.data.MediaInfo;
import fr.rakambda.plexdeleter.api.tautulli.data.MediaPart;
import fr.rakambda.plexdeleter.service.WatchService;
import fr.rakambda.plexdeleter.storage.entity.MediaEntity;
import fr.rakambda.plexdeleter.storage.entity.UserGroupEntity;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Slf4j
public abstract class AbstractNotificationService{
	protected static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	
	private final WatchService watchService;
	private final TautulliService tautulliService;
	
	private final Map<String, Locale> languages;
	
	protected AbstractNotificationService(WatchService watchService, TautulliService tautulliService){
		this.watchService = watchService;
		this.tautulliService = tautulliService;
		
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
			case SEASON -> {
				if(Objects.isNull(media.getPlexId())){
					yield List.of();
				}
				
				var watched = watchService.getGroupWatchHistory(userGroupEntity, media).entrySet().stream()
						.filter(entry -> entry.getValue().stream().anyMatch(r -> Objects.equals(r.getWatchedStatus(), 1)))
						.map(Map.Entry::getKey)
						.toList();
				
				yield IntStream.rangeClosed(1, media.getAvailablePartsCount())
						.filter(index -> !watched.contains(index))
						.mapToObj(String::valueOf)
						.toList();
			}
		};
	}
	
	@NotNull
	public String getTypeKey(@NotNull MediaEntity media){
		return switch(media.getType()){
			case MOVIE -> "mail.watchlist.body.media.movie";
			case SEASON -> "mail.watchlist.body.media.series";
		};
	}
	
	@NotNull
	public String getMediaDuration(@NotNull Duration duration){
		var hours = duration.toHoursPart();
		var minutes = duration.toMinutesPart();
		if(hours <= 0){
			return "%dM".formatted(minutes);
		}
		return "%dH%dM".formatted(hours, minutes);
	}
	
	@NotNull
	protected Optional<byte[]> getPosterData(@NotNull GetMetadataResponse metadata){
		Function<Integer, Optional<byte[]>> posterFunction = ratingKey -> tautulliService.getPosterBytes(ratingKey, 222, 333)
				.filter(d -> d.length > 0);
		
		try{
			return switch(metadata.getMediaType()){
				case "episode" -> posterFunction.apply(metadata.getParentRatingKey()).or(() -> posterFunction.apply(metadata.getGrandparentRatingKey()));
				case "season", "show" -> posterFunction.apply(metadata.getRatingKey()).or(() -> posterFunction.apply(metadata.getParentRatingKey()));
				default -> posterFunction.apply(metadata.getRatingKey());
			};
		}
		catch(Exception e){
			log.warn("Failed to get poster data for {}", metadata, e);
			return Optional.empty();
		}
	}
}
