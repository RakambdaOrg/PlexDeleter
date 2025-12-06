package fr.rakambda.plexdeleter.api.discord.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import fr.rakambda.plexdeleter.json.DiscordFieldValueSerializer;
import lombok.Builder;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

@Builder
@RegisterReflectionForBinding(Field.class)
public record Field(
		@JsonProperty("name") String name,
		@JsonProperty("value") @JsonSerialize(using = DiscordFieldValueSerializer.class) String value,
		@JsonProperty("inline") boolean inline
){
}
