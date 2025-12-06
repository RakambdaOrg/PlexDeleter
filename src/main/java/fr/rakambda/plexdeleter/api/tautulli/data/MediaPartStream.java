package fr.rakambda.plexdeleter.api.tautulli.data;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type", visible = true)
@JsonSubTypes({
		@JsonSubTypes.Type(value = VideoMediaPartStream.class, name = "1"),
		@JsonSubTypes.Type(value = AudioMediaPartStream.class, name = "2"),
		@JsonSubTypes.Type(value = SubtitlesMediaPartStream.class, name = "3"),
})
@RegisterReflectionForBinding(MediaPartStream.class)
public interface MediaPartStream{
}
