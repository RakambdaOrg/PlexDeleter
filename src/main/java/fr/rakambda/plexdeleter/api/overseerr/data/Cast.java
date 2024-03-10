package fr.rakambda.plexdeleter.api.overseerr.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterReflectionForBinding(Cast.class)
public class Cast{
	private int castId;
	private String character;
	private String creditId;
	private int id;
	private String name;
	private int order;
	private int gender;
	private String profilePath;
}
