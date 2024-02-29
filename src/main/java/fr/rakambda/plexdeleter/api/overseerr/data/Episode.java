package fr.rakambda.plexdeleter.api.overseerr.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Episode{
	private int id;
	private LocalDate airDate;
	private int episodeNumber;
	private String name;
	private String overview;
	private String productionCode;
	private int seasonNumber;
	private int showId;
	private Float voteAverage;
	private String stillPath;
}
