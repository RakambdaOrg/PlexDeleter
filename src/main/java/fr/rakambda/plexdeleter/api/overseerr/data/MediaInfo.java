package fr.rakambda.plexdeleter.api.overseerr.data;

import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

@RegisterReflectionForBinding(MediaInfo.class)
public record MediaInfo(
		int id,
		Integer tmdbId,
		Integer tvdbId,
		Integer externalServiceId,
		String externalServiceSlug,
		Integer ratingKey
){
}
