package fr.rakambda.plexdeleter.api.overseerr.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterReflectionForBinding(ExternalIds.class)
public class ExternalIds{
	private String facebookId;
	private String freebaseId;
	private String freebaseMid;
	private String imdbId;
	private String instagramId;
	private Long tvdbId;
	private String tvrageId;
	private String twitterId;
}
