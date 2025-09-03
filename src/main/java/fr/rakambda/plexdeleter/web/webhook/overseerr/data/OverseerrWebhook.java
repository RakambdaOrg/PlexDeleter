package fr.rakambda.plexdeleter.web.webhook.overseerr.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.lang.NonNull;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OverseerrWebhook{
	@NonNull
	@org.jspecify.annotations.NonNull
	@JsonProperty("notification_type")
	private String notificationType;
	@Nullable
	private Media media;
	@Nullable
	private Request request;
	@NonNull
	private Set<Extra> extra = new HashSet<>();
}
