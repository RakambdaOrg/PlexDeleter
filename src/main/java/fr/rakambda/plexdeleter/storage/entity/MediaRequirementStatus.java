package fr.rakambda.plexdeleter.storage.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MediaRequirementStatus{
	ABANDONED(true, false),
	WAITING(false, true),
	WATCHED(true, true);
	
	private final boolean completed;
	private final boolean wantToWatchMore;
}
