package fr.rakambda.plexdeleter.storage.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MediaType{
	MOVIE(fr.rakambda.plexdeleter.api.seerr.data.MediaType.MOVIE, "movie.svg", "media.type.movie", false),
	SEASON(fr.rakambda.plexdeleter.api.seerr.data.MediaType.TV, "tv.svg", "media.type.tv", true);
	
	private final fr.rakambda.plexdeleter.api.seerr.data.MediaType seerrType;
	private final String icon;
	private final String localizationKey;
	private final boolean hasSeasons;
}

