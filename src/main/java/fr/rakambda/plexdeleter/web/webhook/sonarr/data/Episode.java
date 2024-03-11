package fr.rakambda.plexdeleter.web.webhook.sonarr.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Episode{
	private int seasonNumber;
	private int episodeNumber;
}
