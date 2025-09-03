package fr.rakambda.plexdeleter.web.webhook.radarr.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NonNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Movie{
	private int id;
	@NonNull
	private String title;
	@NonNull
	private Integer tmdbId;
}
