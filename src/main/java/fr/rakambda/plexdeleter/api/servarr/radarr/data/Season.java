package fr.rakambda.plexdeleter.api.servarr.radarr.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterReflectionForBinding(Season.class)
public final class Season{
	private int seasonNumber;
	private boolean monitored;
	@NonNull
	private Statistics statistics;
}
