package fr.rakambda.plexdeleter.api.overseerr.data;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

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
	@NotNull
	private Set<RequestSeason> seasons = new HashSet<>();
	private int seasonCount;
	@Nullable
	private RequestMedia media;
	@NotNull
	private User requestedBy;
	@Nullable
	private User modifiedBy;
}
