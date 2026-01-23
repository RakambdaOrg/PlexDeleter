package fr.rakambda.plexdeleter.api.discord.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import fr.rakambda.plexdeleter.json.ColorSerializer;
import fr.rakambda.plexdeleter.json.InstantAsStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Singular;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import tools.jackson.databind.annotation.JsonSerialize;
import java.awt.*;
import java.time.Instant;
import java.util.Collection;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@RegisterReflectionForBinding(Embed.class)
public class Embed{
	@JsonProperty("author")
	private Author author;
	@JsonProperty("title")
	private String title;
	@JsonProperty("url")
	private String url;
	@JsonProperty("description")
	private String description;
	@JsonProperty("color")
	@JsonSerialize(using = ColorSerializer.class)
	private Color color;
	@JsonProperty("fields")
	@Singular
	private Collection<Field> fields;
	@JsonProperty("thumbnail")
	private Image thumbnail;
	@JsonProperty("image")
	private Image image;
	@JsonProperty("footer")
	private Footer footer;
	@JsonProperty("timestamp")
	@JsonSerialize(using = InstantAsStringSerializer.class)
	private Instant timestamp;
}
