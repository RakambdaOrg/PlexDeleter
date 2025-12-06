package fr.rakambda.plexdeleter.api.tautulli.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

@RegisterReflectionForBinding(AudioMediaPartStream.class)
public record AudioMediaPartStream(@JsonProperty("audio_language_code") @Nullable String audioLanguageCode) implements MediaPartStream{
}
