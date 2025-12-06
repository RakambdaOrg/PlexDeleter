package fr.rakambda.plexdeleter.api.tautulli.data;

import lombok.NonNull;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import java.util.HashSet;
import java.util.Set;

@RegisterReflectionForBinding(GetHistoryResponse.class)
public record GetHistoryResponse(
		int recordsFiltered,
		@NonNull Set<HistoryRecord> data
){
	public GetHistoryResponse{
		if(data == null){
			data = new HashSet<>();
		}
	}
}
