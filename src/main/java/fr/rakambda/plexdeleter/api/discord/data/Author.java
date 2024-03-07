package fr.rakambda.plexdeleter.api.discord.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import fr.rakambda.plexdeleter.json.URLSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.net.URL;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
