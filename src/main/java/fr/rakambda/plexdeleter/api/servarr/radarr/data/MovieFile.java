package fr.rakambda.plexdeleter.api.servarr.radarr.data;

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
@RegisterReflectionForBinding({MovieFile.class})
public final class MovieFile{
	private int movieId;
	@NotNull
	private String relativePath;
	@NotNull
	private String path;
	private long size;
	@NotNull
	private Instant dateAdded;
	private int indexerFlags;
	private int customFormatScore;
	private boolean qualityCutoffNotMet;
	private Quality quality;
	private MediaInfo mediaInfo;
	@NotNull
	private Set<Language> languages = new HashSet<>();
	@Nullable
	private String releaseGroup;
	@NotNull
	private String edition;
	private int id;
}
