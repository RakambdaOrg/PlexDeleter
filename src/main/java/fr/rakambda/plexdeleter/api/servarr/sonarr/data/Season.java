package fr.rakambda.plexdeleter.api.servarr.sonarr.data;

import lombok.With;
import org.jspecify.annotations.NonNull;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

@RegisterReflectionForBinding(Season.class)
public record Season(
		int seasonNumber,
		@With boolean monitored,
		@NonNull Statistics statistics
){
}
