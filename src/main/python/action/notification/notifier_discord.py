import logging
from action.notification.common_discord import CommonDiscordNotifier
from action.notification.types.NotifyType import NotifyType
from action.status.user_group_status import UserGroupStatus
from api.discord.discord_helper import DiscordHelper
from api.overseerr.overseerr_helper import OverseerrHelper
from database.media import Media
from database.media_status import MediaStatus
from database.user_group import UserGroup
from typing import Optional


class DiscordNotifier(CommonDiscordNotifier):
    def __init__(self, overseerr: OverseerrHelper, discord: DiscordHelper):
        super().__init__(overseerr, discord)
        self.__discord = discord
        self.__logger = logging.getLogger(__name__)

    def notify(self, user_group: UserGroup, medias: list[Media], user_group_status: Optional[UserGroupStatus], notify_type: NotifyType) -> None:
        parts = user_group.notification_value.split(',') if user_group.notification_value else []
        user_mention = f"<@{parts[0]}>"
        webhook_url = parts[1]

        locale = user_group.locale

        if len(list(filter(lambda m: m.status != MediaStatus.RELEASING, medias))) > 0:
            header = notify_type.get_discord_header(locale)
            self.__discord.send_to(webhook_url, f"{user_mention}\n# {header}")
            for media in filter(lambda m: m.status != MediaStatus.RELEASING, medias):
                self.__discord.send_to(webhook_url, "* " + self._get_markdown_body(locale, media, user_group_status))

        if len(list(filter(lambda m: m.status == MediaStatus.RELEASING, medias))) > 0:
            header_releasing = notify_type.get_discord_header_releasing(locale)
            self.__discord.send_to(webhook_url, f"# {header_releasing}")
            for media in filter(lambda m: m.status == MediaStatus.RELEASING, medias):
                self.__discord.send_to(webhook_url, "* " + self._get_markdown_body(locale, media, user_group_status))

        self.__logger.info("Discord webhook sent")
