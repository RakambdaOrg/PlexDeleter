package fr.rakambda.plexdeleter.api.servarr.data;

import org.jspecify.annotations.NonNull;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import java.util.ArrayList;
import java.util.List;

@RegisterReflectionForBinding(PagedResponse.class)
public record PagedResponse<T>(@NonNull List<T> records){
	public PagedResponse{
		if(records == null){
			records = new ArrayList<>();
		}
	}
}
