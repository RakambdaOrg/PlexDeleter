package fr.rakambda.plexdeleter.api.tautulli.data;

import org.jspecify.annotations.NonNull;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import java.util.ArrayList;
import java.util.List;

@RegisterReflectionForBinding(MediaPart.class)
public record MediaPart(
		int id,
		@NonNull String file,
		@NonNull List<MediaPartStream> streams
){
	public MediaPart{
		if(streams == null){
			streams = new ArrayList<>();
		}
	}
}
