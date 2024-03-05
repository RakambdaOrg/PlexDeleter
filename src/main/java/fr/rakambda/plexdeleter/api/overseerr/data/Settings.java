package fr.rakambda.plexdeleter.api.overseerr.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Settings{
	private int id;
	@NotNull
	private String locale;
	@NotNull
	private String region;
	@Nullable
	private String originalLanguage;
	@Nullable
	private String pgpKey;
	@Nullable
	private String discordId;
	@Nullable
	private String pushbulletAccessToken;
	@Nullable
	private String pushoverApplicationToken;
	@Nullable
	private String pushoverUserKey;
	@Nullable
	private String pushoverSound;
	@Nullable
	private String telegramChatId;
	@Nullable
	private String telegramSendSilently;
	@Nullable
	private String watchlistSyncMovies;
	@Nullable
	private String watchlistSyncTv;
	@NotNull
	private NotificationTypes notificationTypes;
}
