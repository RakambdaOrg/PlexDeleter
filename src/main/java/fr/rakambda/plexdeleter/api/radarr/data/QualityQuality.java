package fr.rakambda.plexdeleter.api.radarr.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public final class QualityQuality{
	private int id;
	private String name;
	private String source;
	private int resolution;
	private String modifier;
}
