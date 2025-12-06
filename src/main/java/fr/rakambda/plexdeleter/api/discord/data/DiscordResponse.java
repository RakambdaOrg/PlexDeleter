package fr.rakambda.plexdeleter.api.discord.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

@RegisterReflectionForBinding(DiscordResponse.class)
public record DiscordResponse(
		@JsonProperty("global") @Nullable Boolean global,
		@JsonProperty("message") @Nullable String message,
		@JsonProperty("retry_after") @Nullable Integer retryAfter,
		@JsonProperty("channel_id") @Nullable Long channelId
){
}
