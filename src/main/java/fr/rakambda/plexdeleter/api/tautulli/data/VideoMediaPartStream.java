package fr.rakambda.plexdeleter.api.tautulli.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@RegisterReflectionForBinding(VideoMediaPartStream.class)
public class VideoMediaPartStream extends MediaPartStream{
	@JsonProperty("video_codec")
	@Nullable
	private String videoCodec;
	@JsonProperty("video_codec_level")
	@Nullable
	private Integer videoCodecLevel;
	@JsonProperty("video_bitrate")
	@Nullable
	private Integer videoBitrate;
	@JsonProperty("video_bit_depth")
	@Nullable
	private Integer videoBitDepth;
	@JsonProperty("video_chroma_subsampling")
	@Nullable
	private String videoChromaSubsampling;
	@JsonProperty("video_color_primaries")
	@Nullable
	private String videoColorPrimaries;
	@JsonProperty("video_color_range")
	@Nullable
	private String videoColorRange;
	@JsonProperty("video_color_space")
	@Nullable
	private String videoColorSpace;
	@JsonProperty("video_color_trc")
	@Nullable
	private String videoColorTrc;
	@JsonProperty("video_dynamic_range")
	@Nullable
	private String videoDynamicRange;
	@JsonProperty("video_frame_rate")
	@Nullable
	private Float videoFrameRate;
	@JsonProperty("video_ref_frames")
	@Nullable
	private Integer videoRefFrames;
	@JsonProperty("video_height")
	@Nullable
	private Integer videoHeight;
	@JsonProperty("video_width")
	@Nullable
	private Integer videoWidth;
	@JsonProperty("video_language")
	@Nullable
	private String videoLanguage;
	@JsonProperty("video_language_code")
	@Nullable
	private String videoLanguageCode;
	@JsonProperty("video_profile")
	@Nullable
	private String videoProfile;
	@JsonProperty("video_scan_type")
	@Nullable
	private String videoScanType;
}
