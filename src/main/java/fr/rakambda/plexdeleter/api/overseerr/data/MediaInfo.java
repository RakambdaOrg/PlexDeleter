package fr.rakambda.plexdeleter.api.overseerr.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MediaInfo{
	private Set<Object> downloadStatus = new HashSet<>();
	private Set<Object> downloadStatus4k = new HashSet<>();
	private int id;
	private String mediaType;
	private Integer tmdbId;
	private Integer tvdbId;
	private Integer imdbId;
	private int status;
	private int status4k;
	private Instant createdAt;
	private Instant updatedAt;
	private Instant lastSeasonChange;
	private Instant mediaAddedAt;
	private int serviceId;
	private int serviceId4k;
	private int externalServiceId;
	private int externalServiceId4k;
	private String externalServiceSlug;
	private String externalServiceSlug4k;
	private Integer ratingKey;
	private String ratingKey4k;
	private Set<Object> requests = new HashSet<>();
	private Set<Object> issues = new HashSet<>();
	private Set<SeasonInfo> seasons = new HashSet<>();
	private String serviceUrl;
}
