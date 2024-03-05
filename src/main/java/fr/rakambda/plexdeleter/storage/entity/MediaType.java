package fr.rakambda.plexdeleter.storage.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MediaType{
	MOVIE(fr.rakambda.plexdeleter.api.overseerr.data.MediaType.MOVIE),
	SEASON(fr.rakambda.plexdeleter.api.overseerr.data.MediaType.TV);
	
	private final fr.rakambda.plexdeleter.api.overseerr.data.MediaType overseerrType;
}

