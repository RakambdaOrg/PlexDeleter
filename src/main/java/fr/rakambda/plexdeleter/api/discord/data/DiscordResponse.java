package fr.rakambda.plexdeleter.api.discord.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.Nullable;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@NoArgsConstructor
@ToString
@RegisterReflectionForBinding(DiscordResponse.class)
public class DiscordResponse{
	@JsonProperty("global")
	private boolean global;
	@JsonProperty("message")
	private String message;
	@JsonProperty("retry_after")
	private int retryAfter;
	@JsonProperty("channel_id")
	@Nullable
	private Long channelId;
}
