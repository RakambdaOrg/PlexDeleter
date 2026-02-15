package fr.rakambda.plexdeleter.api.seerr.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterReflectionForBinding(Collection.class)
public class Collection{
	private int id;
	private String name;
	private String posterPath;
	private String backdropPath;
}
