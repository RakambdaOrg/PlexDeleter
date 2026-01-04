package fr.rakambda.plexdeleter.api.plex.rest.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterReflectionForBinding(Label.class)
public class Label{
	@NonNull
	private Integer id;
	@NonNull
	private String filter;
	@NonNull
	private String tag;
}
