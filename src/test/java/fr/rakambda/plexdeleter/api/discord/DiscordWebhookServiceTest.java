package fr.rakambda.plexdeleter.api.discord;

import fr.rakambda.plexdeleter.api.ClientLoggerRequestInterceptor;
import fr.rakambda.plexdeleter.api.RequestFailedException;
import fr.rakambda.plexdeleter.api.discord.data.Embed;
import fr.rakambda.plexdeleter.api.discord.data.Field;
import fr.rakambda.plexdeleter.api.discord.data.WebhookMessage;
import fr.rakambda.plexdeleter.json.JacksonConfiguration;
import jakarta.validation.constraints.NotBlank;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.validation.annotation.Validated;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@ActiveProfiles("test")
@SpringBootTest(classes = {
		DiscordWebhookApiService.class,
		ClientLoggerRequestInterceptor.class,
		JacksonConfiguration.class
})
@EnableConfigurationProperties(DiscordWebhookServiceTest.WebhookConfiguration.class)
@DisabledIfEnvironmentVariable(named = "CI", matches = "true", disabledReason = "Required service not available on CI")
class DiscordWebhookServiceTest{
	@Validated
	@ConfigurationProperties("discord")
	public record WebhookConfiguration(
			@NonNull @NotBlank String webhook,
			@NonNull @NotBlank String webhookForum
	){
	}
	
	@Autowired
	private WebhookConfiguration webhookConfiguration;
	@Autowired
	private DiscordWebhookApiService tested;
	
	@Test
	void itShouldSendAMessage(){
		var message = WebhookMessage.builder()
				.content("this is a test")
				.build();
		assertThatCode(() -> tested.sendWebhookMessage(webhookConfiguration.webhook, message))
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
		assertThatCode(() -> tested.sendWebhookMessage(webhookConfiguration.webhook, message))
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
		
		var result = tested.sendWebhookMessage(webhookConfiguration.webhookForum, message);
		
		assertThat(result.getChannelId()).isNotNull();
		
		var messageReply = WebhookMessage.builder()
				.content("This is a reply")
				.build();
		
		assertThatCode(() -> tested.sendWebhookMessage(webhookConfiguration.webhookForum, result.getChannelId(), messageReply))
				.doesNotThrowAnyException();
	}
	
	@Test
	void itShouldHandleRateLimit(){
		var message = WebhookMessage.builder()
				.content("Test rate limit")
				.build();
		
		for(var i = 0; i < 30; i++){
			assertThatCode(() -> tested.sendWebhookMessage(webhookConfiguration.webhook, message)).doesNotThrowAnyException();
		}
	}
}