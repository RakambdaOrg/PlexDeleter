from pathlib import Path

from discord_webhook import DiscordWebhook

from database.media import Media
from database.user_group import UserGroup


class DiscordHelper:
    def __init__(self, webhook_url: str):
        self.__webhook_url = webhook_url

    def notify_file_deleted(self, file: Path) -> None:
        self.__send(f"Deleted file {file.resolve()}")

    def notify_media_deleted(self, media: Media):
        self.__send(f"Deleted media {media.id}: {media.name} (Season {media.season_number})")

    def notify_set_finished(self, media: Media) -> None:
        self.__send(f"Marked {media.id} as finished: {media.name} (Season {media.season_number})")

    def notify_watched(self, media: Media, user_group: UserGroup) -> None:
        self.__send(f"{user_group.name} watched {media.name}")

    def __send(self, content) -> None:
        if not self.__webhook_url:
            return

        webhook = DiscordWebhook(url=self.__webhook_url, rate_limit_retry=True, content=content)
        webhook.execute()
