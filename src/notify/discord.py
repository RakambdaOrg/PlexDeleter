from pathlib import Path

from discord_webhook import DiscordWebhook

from database import Database


class Discord:
    def __init__(self, webhook_url: str, database: Database):
        self.__webhook_url = webhook_url
        self.__database = database

    def notify_file_deleted(self, file: Path):
        if not self.__webhook_url:
            return

        webhook = DiscordWebhook(url=self.__webhook_url,
                                 rate_limit_retry=True,
                                 content=f'Deleted file {file.resolve()}')
        webhook.execute()

    def notify_set_finished(self, media_id: int, season_number: int):
        if not self.__webhook_url:
            return

        name = self.__database.get_media_name(media_id)

        webhook = DiscordWebhook(url=self.__webhook_url,
                                 rate_limit_retry=True,
                                 content=f'Marked {media_id} as finished: {name} (Season {season_number})')
        webhook.execute()

    def notify_watched(self, media_id: int, group_id: int):
        if not self.__webhook_url:
            return

        media_name = self.__database.get_media_name(media_id)
        group_name = self.__database.get_group_name(group_id)

        webhook = DiscordWebhook(url=self.__webhook_url,
                                 rate_limit_retry=True,
                                 content=f'{group_name} watched {media_name}')
        webhook.execute()
