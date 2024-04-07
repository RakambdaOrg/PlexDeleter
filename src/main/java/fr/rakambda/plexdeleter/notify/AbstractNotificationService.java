package fr.rakambda.plexdeleter.notify;

import fr.rakambda.plexdeleter.api.RequestFailedException;
import fr.rakambda.plexdeleter.api.tautulli.data.GetMetadataResponse;
import fr.rakambda.plexdeleter.api.tautulli.data.MediaInfo;
import fr.rakambda.plexdeleter.api.tautulli.data.MediaPart;
import fr.rakambda.plexdeleter.service.WatchService;
import fr.rakambda.plexdeleter.storage.entity.MediaEntity;
import fr.rakambda.plexdeleter.storage.entity.UserGroupEntity;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.context.MessageSource;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Slf4j
public abstract class AbstractNotificationService{
	protected static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	
	private final WatchService watchService;
	private final MessageSource messageSource;
	
	protected AbstractNotificationService(WatchService watchService, MessageSource messageSource){
		this.watchService = watchService;
		this.messageSource = messageSource;
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
	
	@Nullable
	protected String getMediaSeason(@NotNull GetMetadataResponse metadata, @NotNull Locale locale){
		return switch(metadata.getMediaType()){
			case EPISODE -> Stream.of(
							Optional.ofNullable(metadata.getParentMediaIndex())
									.map(i -> messageSource.getMessage("mail.media.added.body.season", new Object[]{i}, locale))
									.orElse(null),
							Optional.ofNullable(metadata.getMediaIndex())
									.map(i -> messageSource.getMessage("mail.media.added.body.episode", new Object[]{i}, locale))
									.orElse(null)
					)
					.filter(Objects::nonNull)
					.collect(Collectors.joining(" - "));
			case SHOW -> Optional.ofNullable(metadata.getMediaIndex())
					.map(i -> messageSource.getMessage("mail.media.added.body.season", new Object[]{i}, locale))
					.orElse(null);
			default -> null;
		};
	}
}
