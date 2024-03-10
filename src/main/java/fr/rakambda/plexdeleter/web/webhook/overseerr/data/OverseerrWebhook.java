package fr.rakambda.plexdeleter.web.webhook.overseerr.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.lang.NonNull;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OverseerrWebhook{
	@NonNull
	@NotNull
	@JsonProperty("notification_type")
	private String notificationType;
	@Nullable
	private Media media;
	@Nullable
	private Request request;
	@Nullable
	private Set<Extra> extra;
}
