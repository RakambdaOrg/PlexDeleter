package fr.rakambda.plexdeleter.api.discord;

import org.jspecify.annotations.Nullable;

public class DiscordWebhookException extends Exception{
	public DiscordWebhookException(int status){
		super("Failed to send discord message, got error " + status);
	}
	
	public DiscordWebhookException(@Nullable Throwable e){
		super("Failed to send discord message", e);
	}
}
