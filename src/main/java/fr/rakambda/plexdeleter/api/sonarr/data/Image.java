package fr.rakambda.plexdeleter.api.sonarr.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public final class Image{
	@NotNull
	private String coverType;
	@NotNull
	private String url;
	@NotNull
	private String remoteUrl;
}
