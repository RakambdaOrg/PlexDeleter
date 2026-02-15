package fr.rakambda.plexdeleter.api.seerr.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterReflectionForBinding(Library.class)
public class Library{
	private String id;
	private String name;
	private boolean enabled;
	private String type;
	private Long lastScan;
}
