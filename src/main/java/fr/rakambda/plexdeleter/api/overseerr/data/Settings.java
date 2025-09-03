package fr.rakambda.plexdeleter.api.overseerr.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterReflectionForBinding(Settings.class)
public class Settings{
	private int id;
	@NonNull
	private String locale;
	@NonNull
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
	@NonNull
	private NotificationTypes notificationTypes;
}
