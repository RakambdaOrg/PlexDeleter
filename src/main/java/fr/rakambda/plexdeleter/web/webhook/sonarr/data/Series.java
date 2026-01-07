package fr.rakambda.plexdeleter.web.webhook.sonarr.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Series{
	@Nullable
	private Integer id;
	@NonNull
	private String title;
	@Nullable
	private Integer tvdbId;
}
