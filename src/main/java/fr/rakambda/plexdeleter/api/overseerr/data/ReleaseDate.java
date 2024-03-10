package fr.rakambda.plexdeleter.api.overseerr.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import java.time.Instant;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterReflectionForBinding({ReleaseDate.class})
public class ReleaseDate{
	private String certification;
	private Set<String> descriptors;
	private String iso_639_1;
	private String note;
	@JsonProperty("release_date")
	private Instant releaseDate;
	private int type;
}
