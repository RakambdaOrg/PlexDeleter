package fr.rakambda.plexdeleter.api.tautulli.data;

import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

@RegisterReflectionForBinding(VideoMediaPartStream.class)
public record VideoMediaPartStream() implements MediaPartStream{
}
