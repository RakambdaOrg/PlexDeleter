package fr.rakambda.plexdeleter.api.overseerr.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterReflectionForBinding(PagedResponse.class)
public class PagedResponse<T>{
	private PageInfo pageInfo;
	@NonNull
	private List<T> results = new ArrayList<>();
}
