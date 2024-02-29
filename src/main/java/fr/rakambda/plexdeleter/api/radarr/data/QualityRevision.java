package fr.rakambda.plexdeleter.api.radarr.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public final class QualityRevision{
	private int version;
	private int real;
	@JsonProperty("isRepack")
	private boolean repack;
}
