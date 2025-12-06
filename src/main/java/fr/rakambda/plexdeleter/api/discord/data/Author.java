package fr.rakambda.plexdeleter.api.discord.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import fr.rakambda.plexdeleter.json.URLSerializer;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import java.net.URL;

@RegisterReflectionForBinding(Author.class)
public record Author(
		@JsonProperty("name") String name,
		@JsonProperty("url") @JsonSerialize(using = URLSerializer.class) URL url,
		@JsonProperty("icon_url") @JsonSerialize(using = URLSerializer.class) URL iconUrl
){
}
