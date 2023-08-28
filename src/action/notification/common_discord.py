import logging
from abc import abstractmethod
from typing import Callable, Optional

from action.notification.common_notifier import CommonNotifier
from action.status.user_group_status import UserGroupStatus
from api.discord.discord_helper import DiscordHelper
from api.overseerr.overseerr_helper import OverseerrHelper
from database.media import Media
from database.user_group import UserGroup


class CommonDiscordNotifier(CommonNotifier):
    def __init__(self, overseerr: OverseerrHelper, discord: DiscordHelper):
        self.__overseerr = overseerr
        self.__discord = discord
        self.__logger = logging.getLogger(__name__)

    def notify_watchlist(self, user_group: UserGroup, medias: list[Media], user_group_status: UserGroupStatus) -> None:
        return self._notify(user_group, medias, user_group_status, self._get_header_watchlist, self._get_subject_watchlist)

    def notify_requirement_added(self, user_group: UserGroup, medias: list[Media]) -> None:
        return self._notify(user_group, medias, None, self._get_header_requirement_added, self._get_subject_requirement_added)

    @abstractmethod
    def _notify(self, user_group: UserGroup, medias: list[Media], user_group_status: Optional[UserGroupStatus], header_function: Callable[[str], str], subject_function: Callable[[str], str]) -> None:
        pass

    def _get_markdown_body(self, locale: str, media: Media, user_group_status: Optional[UserGroupStatus]) -> str:
        user_media_status = user_group_status.get(media) if user_group_status else None
        content_parts = [self._get_media_body(locale, media, user_media_status)]

        media_details = self.__overseerr.get_media_details(media.overseerr_id, media.type)
        if media_details.overseerr_url:
            content_parts.append(f"[Overseerr](<{media_details.overseerr_url}>)")
        if media_details.plex_web_url:
            content_parts.append(f"[Plex web](<{media_details.plex_web_url}>)")

        return f"{' | '.join(content_parts)}"
