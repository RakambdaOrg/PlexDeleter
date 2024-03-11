package fr.rakambda.plexdeleter.web.webhook.radarr.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.lang.NonNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RadarrWebhook{
	@NonNull
	@NotNull
	private String eventType;
	@Nullable
	private Movie movie;
}
