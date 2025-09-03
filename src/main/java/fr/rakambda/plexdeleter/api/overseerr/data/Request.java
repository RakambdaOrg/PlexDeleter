package fr.rakambda.plexdeleter.api.overseerr.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterReflectionForBinding(Request.class)
public final class Request{
	private int id;
	private int status;
	@NonNull
	private Instant createdAt;
	@NonNull
	private Instant updatedAt;
	@NonNull
	private String type;
	private boolean is4k;
	@Nullable
	private Integer serverId;
	@Nullable
	private Integer profileId;
	@Nullable
	private String rootFolder;
	@Nullable
	private Integer languageProfileId;
	@Nullable
	private Set<Integer> tags = new HashSet<>();
	private boolean isAutoRequest;
	@NonNull
	private Set<RequestSeason> seasons = new HashSet<>();
	private int seasonCount;
	@Nullable
	private RequestMedia media;
	@NonNull
	private User requestedBy;
	@Nullable
	private User modifiedBy;
}
