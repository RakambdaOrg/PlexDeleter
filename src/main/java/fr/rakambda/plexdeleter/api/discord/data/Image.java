package fr.rakambda.plexdeleter.api.discord.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

@Builder
@RegisterReflectionForBinding(Image.class)
public record Image(@JsonProperty("url") String url){
}
