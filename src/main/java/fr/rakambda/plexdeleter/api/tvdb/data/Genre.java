package fr.rakambda.plexdeleter.api.tvdb.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterReflectionForBinding(Genre.class)
public class Genre{
	private int id;
	@NotNull
	private String name;
	@NotNull
	private String slug;
}
