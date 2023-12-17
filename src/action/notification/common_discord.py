import logging
from typing import Optional

from action.notification.common_notifier import CommonNotifier
from action.status.user_group_status import UserGroupStatus
from api.discord.discord_helper import DiscordHelper
from api.overseerr.overseerr_helper import OverseerrHelper
from database.media import Media


class CommonDiscordNotifier(CommonNotifier):
    def __init__(self, overseerr: OverseerrHelper, discord: DiscordHelper):
        self.__overseerr = overseerr
        self.__discord = discord
        self.__logger = logging.getLogger(__name__)

    def _get_markdown_body(self, locale: str, media: Media, user_group_status: Optional[UserGroupStatus]) -> str:
        user_media_status = user_group_status.get(media) if user_group_status else None
        content_parts = [self._get_media_body(locale, media, user_media_status)]

        media_details = self.__overseerr.get_media_details(media.overseerr_id, media.type)
        if media_details.overseerr_url:
            content_parts.append(f"[Overseerr](<{media_details.overseerr_url}>)")
        if media_details.plex_web_url:
            content_parts.append(f"[Plex web](<{media_details.plex_web_url}>)")

        return f"{' | '.join(content_parts)}"
