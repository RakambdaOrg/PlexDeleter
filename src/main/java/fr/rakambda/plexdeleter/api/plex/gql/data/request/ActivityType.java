package fr.rakambda.plexdeleter.api.plex.gql.data.request;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ActivityType{
	METADATA_MESSAGE("METADATA_MESSAGE"),
	RATING("RATING"),
	WATCH_HISTORY("WATCH_HISTORY"),
	WATCHLIST("WATCHLIST"),
	POST("POST"),
	WATCH_SESSION("WATCH_SESSION"),
	WATCH_RATING("WATCH_RATING"),
	REVIEW("REVIEW"),
	WATCH_REVIEW("WATCH_REVIEW");
	
	private final String value;
}
