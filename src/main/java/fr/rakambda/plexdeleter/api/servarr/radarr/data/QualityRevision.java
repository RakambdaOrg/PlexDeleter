package fr.rakambda.plexdeleter.api.servarr.radarr.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterReflectionForBinding({QualityRevision.class})
public final class QualityRevision{
	private int version;
	private int real;
	@JsonProperty("isRepack")
	private boolean repack;
}
