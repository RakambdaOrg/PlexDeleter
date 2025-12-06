package fr.rakambda.plexdeleter.api.overseerr.data;

import org.jspecify.annotations.NonNull;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import java.util.ArrayList;
import java.util.List;

@RegisterReflectionForBinding(PagedResponse.class)
public record PagedResponse<T>(
		@NonNull List<T> results
){
	public PagedResponse{
		if(results == null){
			results = new ArrayList<>();
		}
	}
}
