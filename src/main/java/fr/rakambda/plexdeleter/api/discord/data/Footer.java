package fr.rakambda.plexdeleter.api.discord.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import fr.rakambda.plexdeleter.json.URLSerializer;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import java.net.URL;

@RegisterReflectionForBinding(Footer.class)
public record Footer(
		@JsonProperty("text") String text,
		@JsonProperty("icon_url") @JsonSerialize(using = URLSerializer.class) URL iconUrl
){
}
