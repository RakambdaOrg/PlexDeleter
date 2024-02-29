package fr.rakambda.plexdeleter.storage.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MediaAvailability{
	DOWNLOADED,
	DOWNLOADING,
	MANUAL
}
