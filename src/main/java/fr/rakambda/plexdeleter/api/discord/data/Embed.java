package fr.rakambda.plexdeleter.api.discord.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import fr.rakambda.plexdeleter.json.ColorSerializer;
import fr.rakambda.plexdeleter.json.InstantAsStringSerializer;
import lombok.Builder;
import lombok.Singular;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import java.awt.*;
import java.time.Instant;
import java.util.Collection;

@Builder
@RegisterReflectionForBinding(Embed.class)
public record Embed(
		@JsonProperty("author") Author author,
		@JsonProperty("title") String title,
		@JsonProperty("url") String url,
		@JsonProperty("description") String description,
		@JsonProperty("color") @JsonSerialize(using = ColorSerializer.class) Color color,
		@JsonProperty("fields") @Singular Collection<Field> fields,
		@JsonProperty("thumbnail") Image thumbnail,
		@JsonProperty("image") Image image,
		@JsonProperty("footer") Footer footer,
		@JsonProperty("timestamp") @JsonSerialize(using = InstantAsStringSerializer.class) Instant timestamp
){
}
