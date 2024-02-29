package fr.rakambda.plexdeleter.api.radarr.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public final class MediaInfo{
	private int audioBitrate;
	private float audioChannels;
	private String audioCodec;
	private String audioLanguages;
	private int audioStreamCount;
	private int videoBitDepth;
	private int videoBitrate;
	private String videoCodec;
	private float videoFps;
	private String videoDynamicRange;
	private String videoDynamicRangeType;
	private String resolution;
	private String runTime;
	private String scanType;
	private String subtitles;
}
