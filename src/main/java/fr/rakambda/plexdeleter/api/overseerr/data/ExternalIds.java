package fr.rakambda.plexdeleter.api.overseerr.data;

import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

@RegisterReflectionForBinding(ExternalIds.class)
public record ExternalIds(
		String imdbId,
		Long tvdbId
){
}
