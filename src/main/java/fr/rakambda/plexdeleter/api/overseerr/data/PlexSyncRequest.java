package fr.rakambda.plexdeleter.api.overseerr.data;

import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

@RegisterReflectionForBinding(PlexSyncRequest.class)
public record PlexSyncRequest(
		boolean cancel,
		boolean start
){
}
