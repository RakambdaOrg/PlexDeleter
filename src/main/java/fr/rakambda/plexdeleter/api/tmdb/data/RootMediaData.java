package fr.rakambda.plexdeleter.api.tmdb.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@RegisterReflectionForBinding(RootMediaData.class)
public sealed abstract class RootMediaData extends MediaData permits MovieData, SeriesData{
	@NonNull
	private List<Genre> genres = new ArrayList<>();
}
