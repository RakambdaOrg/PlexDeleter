import logging

from action.notification.common_notifier import CommonNotifier
from action.status.user_group_status import UserGroupStatus
from api.discord.discord_helper import DiscordHelper
from api.overseerr.overseerr_helper import OverseerrHelper
from database.media import Media
from database.user_group import UserGroup


class DiscordNotifierThread(CommonNotifier):
    def __init__(self, overseerr: OverseerrHelper, discord: DiscordHelper):
        self.__overseerr = overseerr
        self.__discord = discord
        self.__logger = logging.getLogger(__name__)

    def notify(self, user_group: UserGroup, medias: list[Media], user_group_status: UserGroupStatus):
        parts = user_group.notification_value.split(',') if user_group.notification_value else []
        user_mention = f"<@{parts[0]}>"
        webhook_url = parts[1]

        locale = user_group.locale
        header = self._get_header(locale)
        media_texts = ["* " + self.__get_markdown_body(locale, media, user_group_status) for media in medias]

        self.__discord.send_thread(
            webhook_url,
            self.get_subject(locale),
            f"{user_mention}\n# {header}",
            media_texts
        )
        self.__logger.info("Discord webhook sent")

    def __get_markdown_body(self, locale: str, media: Media, user_group_status: UserGroupStatus) -> str:
        content_parts = [self._get_media_body(locale, media, user_group_status.get(media))]

        media_details = self.__overseerr.get_plex_rating_key(media.overseerr_id, media.type)
        if media_details.overseerr_url:
            content_parts.append(f"[Overseerr](<{media_details.overseerr_url}>)")
        if media_details.plex_web_url:
            content_parts.append(f"[Plex web](<{media_details.plex_web_url}>)")

        return f"{' | '.join(content_parts)}"
