package fr.rakambda.plexdeleter.storage.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MediaAvailability{
	DOWNLOADED(true, "media.availability.downloaded"),
	DOWNLOADING(false, "media.availability.downloading"),
	MANUAL(true, "media.availability.manual");
	
	private final boolean available;
	private final String localizationKey;
}
