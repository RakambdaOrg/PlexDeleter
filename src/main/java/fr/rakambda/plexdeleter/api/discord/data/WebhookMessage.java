package fr.rakambda.plexdeleter.api.discord.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import fr.rakambda.plexdeleter.json.URLSerializer;
import lombok.Builder;
import lombok.Singular;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import java.net.URL;
import java.util.Collection;

@Builder
@RegisterReflectionForBinding(WebhookMessage.class)
public record WebhookMessage(
		@JsonProperty("username") String username,
		@JsonProperty("avatar_url") @JsonSerialize(using = URLSerializer.class) URL avatarUrl,
		@JsonProperty("content") String content,
		@JsonProperty("thread_name") String threadName,
		@JsonProperty("embeds") Collection<Embed> embeds,
		@JsonProperty Integer flags,
		@JsonProperty @Singular Collection<Attachment> attachments
){
}
