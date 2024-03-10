package fr.rakambda.plexdeleter.api.servarr.radarr.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterReflectionForBinding(QualityQuality.class)
public final class QualityQuality{
	private int id;
	private String name;
	private String source;
	private int resolution;
	private String modifier;
}
