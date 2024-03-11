package fr.rakambda.plexdeleter.storage.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MediaType{
	MOVIE(fr.rakambda.plexdeleter.api.overseerr.data.MediaType.MOVIE, "movie.svg", "media.type.movie"),
	SEASON(fr.rakambda.plexdeleter.api.overseerr.data.MediaType.TV, "tv.svg", "media.type.tv");
	
	private final fr.rakambda.plexdeleter.api.overseerr.data.MediaType overseerrType;
	private final String icon;
	private final String localizationKey;
}

