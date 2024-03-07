package fr.rakambda.plexdeleter.messaging;

import fr.rakambda.plexdeleter.api.RequestFailedException;
import fr.rakambda.plexdeleter.api.discord.DiscordWebhookService;
import fr.rakambda.plexdeleter.api.discord.data.WebhookMessage;
import fr.rakambda.plexdeleter.config.ApplicationConfiguration;
import fr.rakambda.plexdeleter.config.SupervisionConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SupervisionService{
	private final SupervisionConfiguration supervisionConfiguration;
	private final DiscordWebhookService discordWebhookService;
	
	public SupervisionService(ApplicationConfiguration applicationConfiguration, DiscordWebhookService discordWebhookService){
		this.supervisionConfiguration = applicationConfiguration.getSupervision();
		this.discordWebhookService = discordWebhookService;
	}
	
	public void send(@NotNull String message, @Nullable Object... args){
		var body = message.formatted(args);
		var webhookMessage = WebhookMessage.builder()
				.content(body)
				.build();
		try{
			discordWebhookService.sendWebhookMessage(supervisionConfiguration.getWebhookUrl(), webhookMessage);
		}
		catch(InterruptedException | RequestFailedException e){
			log.error("Failed to send supervision message `{}`", body, e);
		}
	}
	
	@NotNull
	public String sizeToHuman(long size){
		if(size < 1024){
			return size + " B";
		}
		int z = (63 - Long.numberOfLeadingZeros(size)) / 10;
		return "%.1f %sB".formatted((double) size / (1L << (z * 10)), " KMGTPE".charAt(z));
	}
}
