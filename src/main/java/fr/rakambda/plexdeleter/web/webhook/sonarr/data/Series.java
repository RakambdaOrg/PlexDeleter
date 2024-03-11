package fr.rakambda.plexdeleter.web.webhook.sonarr.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Series{
	private int id;
	@NotNull
	private String title;
	@NotNull
	private Integer tvdbId;
	@NotNull
	private List<Episode> episodes = new ArrayList<>();
}
