package fr.rakambda.plexdeleter.api.servarr.sonarr.data;

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
	private int seasonNumber;
}
