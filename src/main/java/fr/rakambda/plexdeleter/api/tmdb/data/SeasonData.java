package fr.rakambda.plexdeleter.api.tmdb.data;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import fr.rakambda.plexdeleter.json.EmptyStringAsNullDeserializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@RegisterReflectionForBinding(SeasonData.class)
public final class SeasonData extends MediaData{
	@Nullable
	@JsonDeserialize(using = EmptyStringAsNullDeserializer.class)
	private String name;
	@NonNull
	private List<EpisodeData> episodes = new ArrayList<>();
}
