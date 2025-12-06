package fr.rakambda.plexdeleter.api.overseerr.data;

import lombok.Data;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

@Data
@RegisterReflectionForBinding(Media.class)
public sealed class Media permits MovieMedia, SeriesMedia{
	private final int id;
	private final String status;
	private final ExternalIds externalIds;
	@Nullable
	private final MediaInfo mediaInfo;
}
