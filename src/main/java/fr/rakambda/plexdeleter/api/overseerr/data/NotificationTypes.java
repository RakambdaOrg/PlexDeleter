package fr.rakambda.plexdeleter.api.overseerr.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationTypes{
	@NotNull
	private String email;
	private long discord;
	private long pushbullet;
	private long pushover;
	private long slack;
	private long telegram;
	private long webhook;
	private long webpush;
}
