package fr.rakambda.plexdeleter.api.seerr.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterReflectionForBinding(RequestMedia.class)
public class RequestMedia{
	private Integer tmdbId;
	private Integer tvdbId;
	private Integer ratingKey;
	@NonNull
	private MediaType mediaType;
}
