package fr.rakambda.plexdeleter.api.tvdb.data;

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
@RegisterReflectionForBinding(SeriesData.class)
public class SeriesData extends MediaData{
	@NonNull
	private List<EpisodeData> episodes = new ArrayList<>();
}
