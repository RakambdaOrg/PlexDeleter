package fr.rakambda.plexdeleter.api.servarr.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterReflectionForBinding(OriginalLanguage.class)
public final class OriginalLanguage{
	private int id;
	@NotNull
	private String name;
}
