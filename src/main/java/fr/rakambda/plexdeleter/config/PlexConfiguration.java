package fr.rakambda.plexdeleter.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.lang.NonNull;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlexConfiguration{
	@NotNull
	@NonNull
	private String endpoint;
	@NotNull
	@NonNull
	private String appEndpoint;
	@NotNull
	@NonNull
	private String serverId;
	@Nullable
	private List<Integer> temporaryLibraries;
}
