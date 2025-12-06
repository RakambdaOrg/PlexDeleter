package fr.rakambda.plexdeleter.api.overseerr.data;

import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

@RegisterReflectionForBinding(PlexSyncResponse.class)
public record PlexSyncResponse(boolean running){
}
