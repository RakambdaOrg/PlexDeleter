package fr.rakambda.plexdeleter.api.overseerr.data;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import java.util.HashSet;
import java.util.Set;

@RegisterReflectionForBinding(Request.class)
public record Request(
		int id,
		@NonNull Set<Integer> tags,
		@NonNull Set<RequestSeason> seasons,
		@Nullable RequestMedia media,
		@NonNull User requestedBy
){
	public Request{
		if(tags == null){
			tags = new HashSet<>();
		}
		if(seasons == null){
			seasons = new HashSet<>();
		}
	}
}
