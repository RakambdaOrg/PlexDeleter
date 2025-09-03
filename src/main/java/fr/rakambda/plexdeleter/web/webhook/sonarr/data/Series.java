package fr.rakambda.plexdeleter.web.webhook.sonarr.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NonNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Series{
	private int id;
	@NonNull
	private String title;
	@NonNull
	private Integer tvdbId;
}
