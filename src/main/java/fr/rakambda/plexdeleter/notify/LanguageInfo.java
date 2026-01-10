package fr.rakambda.plexdeleter.notify;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public record LanguageInfo(
		@NonNull String translationKey,
		@Nullable String flagUrl
){
}
