package fr.rakambda.plexdeleter.storage.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MediaAvailability{
	DOWNLOADED(true, false, false, "media.availability.downloaded", "table-success"),
	DOWNLOADED_NEED_METADATA(true, false, false, "media.availability.downloaded", "table-success"),
	DOWNLOADING(false, true, false, "media.availability.downloading", "table-warning"),
	WAITING(false, false, true, "media.availability.waiting", ""),
	MANUAL(false, true, false, "media.availability.manual", "table-info");
	
	private final boolean available;
	private final boolean inProgress;
	private final boolean waiting;
	private final String localizationKey;
	private final String tableClass;
}
