package fr.rakambda.plexdeleter.web.webhook.sonarr.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.lang.NonNull;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SonarrWebhook{
	@NonNull
	@NotNull
	private String eventType;
	@Nullable
	private Series series;
	@NotNull
	private List<Episode> episodes = new ArrayList<>();
}
