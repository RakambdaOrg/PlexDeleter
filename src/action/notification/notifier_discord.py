import logging
from typing import Callable, Optional

from action.notification.common_discord import CommonDiscordNotifier
from action.status.user_group_status import UserGroupStatus
from api.discord.discord_helper import DiscordHelper
from api.overseerr.overseerr_helper import OverseerrHelper
from database.media import Media
from database.user_group import UserGroup


class DiscordNotifier(CommonDiscordNotifier):
    def __init__(self, overseerr: OverseerrHelper, discord: DiscordHelper):
        super().__init__(overseerr, discord)
        self.__discord = discord
        self.__logger = logging.getLogger(__name__)

    def _notify(self, user_group: UserGroup, medias: list[Media], user_group_status: Optional[UserGroupStatus], header_function: Callable[[str], str], subject_function: Callable[[str], str]) -> None:
        parts = user_group.notification_value.split(',') if user_group.notification_value else []
        user_mention = f"<@{parts[0]}>"
        webhook_url = parts[1]

        locale = user_group.locale
        header = header_function(locale)

        self.__discord.send_to(webhook_url, f"{user_mention}\n# {header}")
        for media in medias:
            self.__discord.send_to(webhook_url, "* " + self._get_markdown_body(locale, media, user_group_status))
        self.__logger.info("Discord webhook sent")
