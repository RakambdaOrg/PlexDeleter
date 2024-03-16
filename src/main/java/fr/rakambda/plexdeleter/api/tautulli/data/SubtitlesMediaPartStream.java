package fr.rakambda.plexdeleter.api.tautulli.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@RegisterReflectionForBinding(SubtitlesMediaPartStream.class)
public class SubtitlesMediaPartStream extends MediaPartStream{
	@JsonProperty("subtitle_codec")
	@Nullable
	private String subtitleCodec;
	@JsonProperty("subtitle_container")
	@Nullable
	private String subtitleContainer;
	@JsonProperty("subtitle_format")
	@Nullable
	private String subtitleFormat;
	@JsonProperty("subtitle_forced")
	private int subtitleForced;
	@JsonProperty("subtitle_location")
	@Nullable
	private String subtitleLocation;
	@JsonProperty("subtitle_language")
	@Nullable
	private String subtitleLanguage;
	@JsonProperty("subtitle_language_code")
	@Nullable
	private String subtitleLanguageCode;
}
