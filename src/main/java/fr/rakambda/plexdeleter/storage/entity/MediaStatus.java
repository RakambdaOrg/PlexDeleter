package fr.rakambda.plexdeleter.storage.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

@Getter
@RequiredArgsConstructor
public enum MediaStatus{
	WAITING(true,
			false,
			false,
			false,
			false,
			false,
			"media.availability.waiting",
			""),
	DOWNLOADING(true,
			false,
			true,
			false,
			false,
			false,
			"media.availability.downloading",
			"table-warning"),
	DOWNLOADED_NEED_METADATA(true,
			false,
			true,
			true,
			false,
			false,
			"media.availability.downloaded",
			"table-success"),
	DOWNLOADED(false,
			true,
			true,
			true,
			false,
			false,
			"media.availability.downloaded",
			"table-success"),
	PENDING_DELETION(false,
			false,
			true,
			true,
			true,
			false,
			"media.availability.pending-deletion",
			"table-success"),
	DELETED(false,
			false,
			false,
			false,
			false,
			false,
			"media.availability.deleted",
			"table-success"),
	MANUAL(false,
			false,
			false,
			false,
			false,
			true,
			"media.availability.manual",
			"table-info"),
	KEEP(false,
			false,
			false,
			true,
			false,
			true,
			"media.availability.keep",
			"table-info");
	
	private final boolean needsMetadataRefresh;
	private final boolean needsRequirementsRefresh;
	private final boolean downloadStarted;
	private final boolean fullyDownloaded;
	private final boolean canBeDeleted;
	private final boolean neverChange;
	private final String localizationKey;
	private final String tableClass;
	
	private static Collection<MediaStatus> ALL_NEED_REFRESH;
	
	@NotNull
	public static Collection<MediaStatus> allAvailable(){
		if(Objects.isNull(ALL_NEED_REFRESH)){
			ALL_NEED_REFRESH = Arrays.stream(MediaStatus.values())
					.filter(s -> s.isDownloadStarted() || s.isNeedsMetadataRefresh())
					.toList();
		}
		return ALL_NEED_REFRESH;
	}
	
	@NotNull
	public static Collection<MediaStatus> allNeedRefresh(){
		if(Objects.isNull(ALL_NEED_REFRESH)){
			ALL_NEED_REFRESH = Arrays.stream(MediaStatus.values())
					.filter(s -> s.isNeedsMetadataRefresh() || s.isNeedsRequirementsRefresh())
					.toList();
		}
		return ALL_NEED_REFRESH;
	}
}
