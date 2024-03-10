package fr.rakambda.plexdeleter.api.overseerr.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterReflectionForBinding(ContentRating.class)
public class ContentRating{
	@Nullable
	private Set<String> descriptors;
	private String iso_3166_1;
	private String rating;
}
