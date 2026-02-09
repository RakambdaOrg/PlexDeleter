package fr.rakambda.plexdeleter.api.tautulli.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterReflectionForBinding(GetHistoryResponse.class)
public class GetHistoryResponse{
	private int recordsFiltered;
	@NonNull
	private Set<HistoryRecord> data = new HashSet<>();
}
