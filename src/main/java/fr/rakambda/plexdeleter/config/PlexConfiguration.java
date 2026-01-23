package fr.rakambda.plexdeleter.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlexConfiguration{
	@NonNull
	private String endpoint;
	@NonNull
	private String appEndpoint;
	@NonNull
	private String communityEndpoint;
	@NonNull
	private String communityToken;
	@NonNull
	private String pmsEndpoint;
	@NonNull
	private String pmsToken;
	@NonNull
	private String serverId;
	@Nullable
	private List<Integer> temporaryLibraries;
}
