package fr.rakambda.plexdeleter.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.lang.NonNull;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlexConfiguration{
	@NonNull
	@org.jspecify.annotations.NonNull
	private String endpoint;
	@NonNull
	@org.jspecify.annotations.NonNull
	private String appEndpoint;
	@NonNull
	@org.jspecify.annotations.NonNull
	private String communityEndpoint;
	@NonNull
	@org.jspecify.annotations.NonNull
	private String communityToken;
	@NonNull
	@org.jspecify.annotations.NonNull
	private String serverId;
	@Nullable
	private List<Integer> temporaryLibraries;
}
