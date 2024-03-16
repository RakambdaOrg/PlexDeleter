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
@RegisterReflectionForBinding(AudioMediaPartStream.class)
public class AudioMediaPartStream extends MediaPartStream{
	@JsonProperty("audio_codec")
	@Nullable
	private String audioCodec;
	@JsonProperty("audio_bitrate")
	@Nullable
	private Integer audioBitrate;
	@JsonProperty("audio_bitrate_mode")
	@Nullable
	private String audioBitrateMode;
	@JsonProperty("audio_channels")
	@Nullable
	private Integer audioChannels;
	@JsonProperty("audio_channel_layout")
	@Nullable
	private String audioChannelLayout;
	@JsonProperty("audio_sample_rate")
	@Nullable
	private Integer audioSampleRate;
	@JsonProperty("audio_language")
	@Nullable
	private String audioLanguage;
	@JsonProperty("audio_language_code")
	@Nullable
	private String audioLanguageCode;
	@JsonProperty("audio_profile")
	@Nullable
	private String audioProfile;
}
