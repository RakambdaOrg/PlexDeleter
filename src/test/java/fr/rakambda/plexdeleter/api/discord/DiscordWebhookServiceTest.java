package fr.rakambda.plexdeleter.api.discord;

import fr.rakambda.plexdeleter.SecretsUtils;
import fr.rakambda.plexdeleter.api.RequestFailedException;
import fr.rakambda.plexdeleter.api.discord.data.Embed;
import fr.rakambda.plexdeleter.api.discord.data.Field;
import fr.rakambda.plexdeleter.api.discord.data.WebhookMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@DisabledIfEnvironmentVariable(named = "CI", matches = "true", disabledReason = "Required service not available on CI")
class DiscordWebhookServiceTest{
	private DiscordWebhookApiService tested = new DiscordWebhookApiService();
	
	@Test
	void itShouldSendAMessage(){
		var message = WebhookMessage.builder()
				.content("this is a test")
				.build();
		assertThatCode(() -> tested.sendWebhookMessage(SecretsUtils.getSecret("discord.webhook"), message))
				.doesNotThrowAnyException();
	}
	
	@Test
	void itShouldSendAnEmbed(){
		var message = WebhookMessage.builder()
				.embeds(List.of(Embed.builder()
						.title("test title")
						.description("From test")
						.field(Field.builder().name("f1").value("v1").build())
						.build()))
				.build();
		assertThatCode(() -> tested.sendWebhookMessage(SecretsUtils.getSecret("discord.webhook"), message))
				.doesNotThrowAnyException();
	}
	
	@Test
	void itShouldSendInAForum() throws RequestFailedException, InterruptedException{
		var message = WebhookMessage.builder()
				.embeds(List.of(Embed.builder()
						.title("test title")
						.description("From test")
						.field(Field.builder().name("f1").value("v1").build())
						.build()))
				.threadName("test thread")
				.build();
		
		var result = tested.sendWebhookMessage(SecretsUtils.getSecret("discord.webhook-forum"), message);
		
		assertThat(result.getChannelId()).isNotNull();
		
		var messageReply = WebhookMessage.builder()
				.content("This is a reply")
				.build();
		
		assertThatCode(() -> tested.sendWebhookMessage(SecretsUtils.getSecret("discord.webhook-forum"), result.getChannelId(), messageReply))
				.doesNotThrowAnyException();
	}
	
	@Test
	void itShouldHandleRateLimit(){
		var webhook = SecretsUtils.getSecret("discord.webhook");
		var message = WebhookMessage.builder()
				.content("Test rate limit")
				.build();
		
		for(var i = 0; i < 30; i++){
			assertThatCode(() -> tested.sendWebhookMessage(webhook, message)).doesNotThrowAnyException();
		}
	}
}