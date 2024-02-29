package fr.rakambda.plexdeleter.api.radarr.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public final class Quality{
	@NotNull
	private QualityQuality quality;
	@NotNull
	private QualityRevision revision;
}
