from pathlib import Path

from discord_webhook import DiscordWebhook


class Discord:
    def __init__(self, webhook_url: str):
        self.__webhook_url = webhook_url

    def notify_file_deleted(self, file: Path):
        if not self.__webhook_url:
            return
        
        webhook = DiscordWebhook(url=self.__webhook_url,
                                 rate_limit_retry=True,
                                 content=f'Deleted file {file.resolve()}')
        webhook.execute()
