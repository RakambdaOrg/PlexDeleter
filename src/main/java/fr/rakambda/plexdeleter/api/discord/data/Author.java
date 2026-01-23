package fr.rakambda.plexdeleter.api.discord.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import fr.rakambda.plexdeleter.json.URLSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import tools.jackson.databind.annotation.JsonSerialize;
import java.net.URL;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@RegisterReflectionForBinding(Author.class)
public class Author{
	@JsonProperty("name")
	private String name;
	@JsonProperty("url")
	@JsonSerialize(using = URLSerializer.class)
	private URL url;
	@JsonProperty("icon_url")
	@JsonSerialize(using = URLSerializer.class)
	private URL iconUrl;
}
