package fr.rakambda.plexdeleter.storage.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MediaAvailability{
	DOWNLOADED(true, "media.availability.downloaded", "table-success"),
	DOWNLOADING(false, "media.availability.downloading", "table-warning"),
	WAITING(false, "media.availability.waiting", ""),
	MANUAL(true, "media.availability.manual", "table-info");
	
	private final boolean available;
	private final String localizationKey;
	private final String tableClass;
}
