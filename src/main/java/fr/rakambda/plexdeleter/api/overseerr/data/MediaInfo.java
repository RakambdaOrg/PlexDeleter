package fr.rakambda.plexdeleter.api.overseerr.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import java.time.Instant;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterReflectionForBinding(MediaInfo.class)
public class MediaInfo{
	@Nullable
	private Set<Object> downloadStatus;
	@Nullable
	private Set<Object> downloadStatus4k;
	private int id;
	private MediaType mediaType;
	private Integer tmdbId;
	private Integer tvdbId;
	private Integer imdbId;
	private int status;
	private int status4k;
	private Instant createdAt;
	private Instant updatedAt;
	private Instant lastSeasonChange;
	private Instant mediaAddedAt;
	private Integer serviceId;
	private Integer serviceId4k;
	private Integer externalServiceId;
	private Integer externalServiceId4k;
	private String externalServiceSlug;
	private String externalServiceSlug4k;
	private Integer ratingKey;
	private String ratingKey4k;
	@Nullable
	private Set<Object> requests;
	@Nullable
	private Set<Object> issues;
	@Nullable
	private Set<SeasonInfo> seasons;
	private String serviceUrl;
}
