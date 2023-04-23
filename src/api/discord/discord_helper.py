from pathlib import Path

import requests
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
        thread_response = requests.post(
            url=url,
            json={
                "content": thread_original_post,
                "thread_name": thread_name
            },
            params={
                "wait": "true"
            }
        ).json()

        thread_id = thread_response["channel_id"]

        for thread_message in thread_messages:
            requests.post(
                url=url,
                json={
                    "content": thread_message,
                },
                params={
                    "wait": "true",
                    "thread_id": thread_id
                }
            )
