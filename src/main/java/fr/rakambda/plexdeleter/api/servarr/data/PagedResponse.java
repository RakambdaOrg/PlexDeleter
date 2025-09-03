package fr.rakambda.plexdeleter.api.servarr.data;

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
	private int page;
	private int pageSize;
	@NonNull
	private String sortKey;
	@NonNull
	private String sortDirection;
	private int totalRecords;
	@NonNull
	private List<T> records = new ArrayList<>();
}
