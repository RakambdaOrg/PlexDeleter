package fr.rakambda.plexdeleter.api.discord.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import fr.rakambda.plexdeleter.json.DiscordFieldValueSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@RegisterReflectionForBinding(Field.class)
public class Field{
	@JsonProperty("name")
	private String name;
	@JsonProperty("value")
	@JsonSerialize(using = DiscordFieldValueSerializer.class)
	private String value;
	@JsonProperty("inline")
	private boolean inline;
}
