package fr.rakambda.plexdeleter.api.overseerr.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Season{
	private LocalDate airDate;
	private int episodeCount;
	private int id;
	private String name;
	private String overview;
	private int seasonNumber;
	private String posterPath;
}
