package fr.rakambda.plexdeleter.api.seerr.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@RegisterReflectionForBinding(MovieMedia.class)
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
