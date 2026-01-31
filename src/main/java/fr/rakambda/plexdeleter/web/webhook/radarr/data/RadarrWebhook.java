package fr.rakambda.plexdeleter.web.webhook.radarr.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RadarrWebhook{
	@NonNull
	private String eventType;
	@Nullable
	private Movie movie;
}
