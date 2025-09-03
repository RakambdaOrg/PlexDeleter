package fr.rakambda.plexdeleter.web.webhook.radarr.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.lang.NonNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RadarrWebhook{
	@NonNull
	@org.jspecify.annotations.NonNull
	private String eventType;
	@Nullable
	private Movie movie;
}
