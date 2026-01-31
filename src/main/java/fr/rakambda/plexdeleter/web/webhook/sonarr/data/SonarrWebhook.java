package fr.rakambda.plexdeleter.web.webhook.sonarr.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SonarrWebhook{
	@NonNull
	private String eventType;
	@Nullable
	private Series series;
	@NonNull
	private List<Episode> episodes = new ArrayList<>();
}
