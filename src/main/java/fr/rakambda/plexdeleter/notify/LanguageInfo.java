package fr.rakambda.plexdeleter.notify;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

@RegisterReflectionForBinding(LanguageInfo.class)
public record LanguageInfo(
		@NonNull String translationKey,
		@Nullable String flagUrl
){
}
