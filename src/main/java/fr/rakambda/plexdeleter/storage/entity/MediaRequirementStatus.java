package fr.rakambda.plexdeleter.storage.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MediaRequirementStatus{
	ABANDONED(true),
	WAITING(false),
	WATCHED(true);
	
	private final boolean completed;
}
