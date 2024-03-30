package fr.rakambda.plexdeleter.api.overseerr.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
	@NotNull
	private Instant createdAt;
	@NotNull
	private Instant updatedAt;
	@NotNull
	private String type;
	private boolean is4k;
	private int serverId;
	private int profileId;
	@Nullable
	private String rootFolder;
	@Nullable
	private Integer languageProfileId;
	@Nullable
	private Set<Integer> tags = new HashSet<>();
	private boolean isAutoRequest;
	@NotNull
	private Set<RequestSeason> seasons = new HashSet<>();
	private int seasonCount;
	@NotNull
	private RequestMedia media;
	@NotNull
	private User requestedBy;
	@NotNull
	private User modifiedBy;
}
