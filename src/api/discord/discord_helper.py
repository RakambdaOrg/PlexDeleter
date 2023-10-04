import json
from pathlib import Path

from discord_webhook import DiscordWebhook

from database.media import Media
from database.user_group import UserGroup


class DiscordHelper:
    def __init__(self, webhook_url: str):
        self.__webhook_url = webhook_url

    def notify_file_deleted(self, file: Path) -> None:
        self.__send(f" * Deleted file {file.resolve()}")

    def notify_media_deleted(self, media: Media):
        self.__send(f"🗑️ Deleted media {media.id}: {media}")

    def notify_media_added(self, media: Media):
        self.__send(f"✅ Added media {media.id}: {media}")

    def notify_media_requirement_added(self, media: Media, user_group: UserGroup):
        self.__send(f"📋 Added media requirement for {user_group.name} on {media.id}: {media}")

    def notify_set_finished(self, media: Media, element_count: int, total_element_count: int) -> None:
        self.__send(f"🆗 Marked {media.id} as finished: {media} ({element_count}/{total_element_count})")

    def notify_watched(self, media: Media, user_group: UserGroup) -> None:
        self.__send(f"👁️ {user_group.name} watched {media}")

    def notify_cannot_delete(self, media: Media):
        self.__send(f"❌ Could not delete because no rating key {media}")

    def notify_cannot_update(self, media: Media):
        self.__send(f"❌ Could not update status because no rating key {media}")

    def notify_cannot_update_watch(self, media: Media):
        self.__send(f"❌ Could not update watch status because no rating key {media}")

    def __send(self, content: str) -> None:
        if not self.__webhook_url:
            return

        self.send_to(self.__webhook_url, content)

    @staticmethod
    def send_to(url: str, content: str) -> None:
        webhook = DiscordWebhook(url=url, rate_limit_retry=True, content=content)
        webhook.execute()

    @staticmethod
    def send_thread(url: str, thread_name: str, thread_original_post: str, thread_messages: list[str]) -> None:
        webhook = DiscordWebhook(url=url, rate_limit_retry=True, content=thread_original_post, thread_name=thread_name)
        response = webhook.execute()
        thread_id = json.loads(response.content.decode("utf-8")).get("channel_id")

        for thread_message in thread_messages:
            webhook = DiscordWebhook(url=url, rate_limit_retry=True, content=thread_message, thread_id=thread_id)
            webhook.execute()
