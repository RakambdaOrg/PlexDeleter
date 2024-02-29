package fr.rakambda.plexdeleter.api.tautulli.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MediaPart{
	private int id;
	@NotNull
	private String file;
	@JsonProperty("file_size")
	private long fileSize;
}
