package fr.rakambda.plexdeleter.api.servarr.radarr.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterReflectionForBinding(Statistics.class)
public final class Statistics{
	private int movieFileCount;
	private long sizeOnDisk;
	@NotNull
	private Set<String> releaseGroups = new HashSet<>();
}
