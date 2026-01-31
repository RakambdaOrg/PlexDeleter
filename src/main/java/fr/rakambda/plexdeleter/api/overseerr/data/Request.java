package fr.rakambda.plexdeleter.api.overseerr.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterReflectionForBinding(Request.class)
public final class Request{
	private int id;
	@Nullable
	private Set<Integer> tags = new HashSet<>();
	@NonNull
	private Set<RequestSeason> seasons = new HashSet<>();
	@Nullable
	private RequestMedia media;
	@NonNull
	private User requestedBy;
	@Nullable
	private User modifiedBy;
}
