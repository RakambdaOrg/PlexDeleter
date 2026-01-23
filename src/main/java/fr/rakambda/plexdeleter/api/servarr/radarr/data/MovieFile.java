package fr.rakambda.plexdeleter.api.servarr.radarr.data;

import fr.rakambda.plexdeleter.json.InstantAsStringWithoutNanosSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import tools.jackson.databind.annotation.JsonSerialize;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterReflectionForBinding(MovieFile.class)
public final class MovieFile{
	private int movieId;
	@NonNull
	private String relativePath;
	@NonNull
	private String path;
	private long size;
	@NonNull
	@JsonSerialize(using = InstantAsStringWithoutNanosSerializer.class)
	private Instant dateAdded;
	private int indexerFlags;
	private Integer customFormatScore;
	private boolean qualityCutoffNotMet;
	private Quality quality;
	private MediaInfo mediaInfo;
	@NonNull
	private Set<Language> languages = new HashSet<>();
	@Nullable
	private String releaseGroup;
	@NonNull
	private String edition;
	private int id;
}
