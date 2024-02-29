package fr.rakambda.plexdeleter.api.radarr.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public final class Language{
	@NotNull
	private String name;
	private int id;
}
