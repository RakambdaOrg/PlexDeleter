package fr.rakambda.plexdeleter.api.tautulli.data;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

@Getter
@RequiredArgsConstructor
@RegisterReflectionForBinding(MediaType.class)
public enum MediaType{
	ARTIST("artist", false, "", ""),
	EPISODE("episode", true, "episode.svg", "media.type.episode"),
	MOVIE("movie", true, "movie.svg", "media.type.movie"),
	PHOTO("photo", false, "", ""),
	SEASON("season", true, "tv.svg", "media.type.tv"),
	SHOW("show", true, "episode.svg", "media.type.episode"),
	TRACK("track", false, "", "");
	
	@JsonValue
	private final String value;
	private final boolean notifyAdded;
	private final String icon;
	private final String localizationKey;
}
