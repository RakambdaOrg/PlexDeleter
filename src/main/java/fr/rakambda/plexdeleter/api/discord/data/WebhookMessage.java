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
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import java.net.URL;
import java.util.Collection;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@RegisterReflectionForBinding({WebhookMessage.class})
public class WebhookMessage{
	@JsonProperty("username")
	private String username;
	@JsonProperty("avatar_url")
	@JsonSerialize(using = URLSerializer.class)
	private URL avatarUrl;
	@JsonProperty("content")
	private String content;
	@JsonProperty("thread_name")
	private String threadName;
	@JsonProperty("embeds")
	private Collection<Embed> embeds;
}
