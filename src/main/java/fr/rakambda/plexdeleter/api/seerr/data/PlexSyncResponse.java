package fr.rakambda.plexdeleter.api.seerr.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import java.util.LinkedList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterReflectionForBinding(PlexSyncResponse.class)
public class PlexSyncResponse{
	private boolean running;
	private int progress;
	private int total;
	private Library currentLibrary;
	private List<Library> libraries = new LinkedList<>();
}
