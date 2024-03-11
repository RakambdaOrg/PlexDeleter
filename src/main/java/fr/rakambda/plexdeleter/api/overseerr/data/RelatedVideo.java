package fr.rakambda.plexdeleter.api.overseerr.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterReflectionForBinding(RelatedVideo.class)
public class RelatedVideo{
	private String site;
	private String key;
	private String name;
	private int size;
	private String type;
	private String url;
}
