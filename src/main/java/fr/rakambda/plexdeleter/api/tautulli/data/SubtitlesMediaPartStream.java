package fr.rakambda.plexdeleter.api.tautulli.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

@RegisterReflectionForBinding(SubtitlesMediaPartStream.class)
public record SubtitlesMediaPartStream(@JsonProperty("subtitle_language_code") @Nullable String subtitleLanguageCode) implements MediaPartStream{
}
