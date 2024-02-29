package fr.rakambda.plexdeleter.api.radarr.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public final class AlternativeTitle{
	@NotNull
	private String title;
	@NotNull
	private String sourceType;
	private int id;
	private int movieMetadataId;
}
