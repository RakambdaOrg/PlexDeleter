package fr.rakambda.plexdeleter.api.servarr.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterReflectionForBinding(Image.class)
public final class Image{
	@NonNull
	private String coverType;
	@NonNull
	private String url;
	@NonNull
	private String remoteUrl;
}
