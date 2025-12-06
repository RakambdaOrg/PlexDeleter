package fr.rakambda.plexdeleter.api.tautulli.data;

import lombok.NonNull;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import java.util.HashSet;
import java.util.Set;

@RegisterReflectionForBinding(GetLibraryMediaInfo.class)
public record GetLibraryMediaInfo(@NonNull Set<MediaRecord> data){
	public GetLibraryMediaInfo{
		if(data == null){
			data = new HashSet<>();
		}
	}
}
