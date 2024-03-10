package fr.rakambda.plexdeleter.api.overseerr.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterReflectionForBinding(Release.class)
public class Release{
	private String iso_3166_1;
	@JsonProperty("release_dates")
	private Set<ReleaseDate> releaseDates;
}
