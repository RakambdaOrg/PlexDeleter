package fr.rakambda.plexdeleter.api.tautulli.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import fr.rakambda.plexdeleter.json.EmptyStringAsNullDeserializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import tools.jackson.databind.annotation.JsonDeserialize;

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
	@JsonDeserialize(using = EmptyStringAsNullDeserializer.class)
	private String subtitleContainer;
	@JsonProperty("subtitle_format")
	@Nullable
	@JsonDeserialize(using = EmptyStringAsNullDeserializer.class)
	private String subtitleFormat;
	@JsonProperty("subtitle_forced")
	private int subtitleForced;
	@JsonProperty("subtitle_location")
	@Nullable
	private String subtitleLocation;
	@JsonProperty("subtitle_language")
	@Nullable
	@JsonDeserialize(using = EmptyStringAsNullDeserializer.class)
	private String subtitleLanguage;
	@JsonProperty("subtitle_language_code")
	@Nullable
	@JsonDeserialize(using = EmptyStringAsNullDeserializer.class)
	private String subtitleLanguageCode;
}
