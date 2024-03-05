package fr.rakambda.plexdeleter.api.overseerr.data;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@JsonTypeName("movie")
public final class MovieMedia extends Media{
	private boolean adult;
	private Long budget;
	private String originalTitle;
	private LocalDate releaseDate;
	private Releases releases;
	private Long revenue;
	private String title;
	private boolean video;
	private String imdbId;
	private Integer runtime;
	private Collection collection;
}
