package fr.rakambda.plexdeleter.api.tvdb.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterReflectionForBinding(Genre.class)
public class Genre{
	private int id;
	@NonNull
	private String name;
	@NonNull
	private String slug;
}
