package fr.rakambda.plexdeleter.api.tautulli.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import java.util.LinkedList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterReflectionForBinding(MediaPart.class)
public class MediaPart{
	private int id;
	@NonNull
	private String file;
	@JsonProperty("file_size")
	private long fileSize;
	@NonNull
	private List<MediaPartStream> streams = new LinkedList<>();
}
