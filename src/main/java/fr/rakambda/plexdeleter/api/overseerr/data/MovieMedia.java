package fr.rakambda.plexdeleter.api.overseerr.data;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

@Value
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@RegisterReflectionForBinding(MovieMedia.class)
public class MovieMedia extends Media{
	String title;
	
	public MovieMedia(int id, String status, ExternalIds externalIds, @Nullable MediaInfo mediaInfo, String title){
		super(id, status, externalIds, mediaInfo);
		this.title = title;
	}
}
