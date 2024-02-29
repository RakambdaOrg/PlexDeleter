import logging
import threading
from action.deleter import Deleter
from action.notifier import Notifier
from action.status.user_group_status import UserGroupStatus
from action.status_updater import StatusUpdater
from action.watch_updater import WatchUpdater
from api.discord.discord_helper import DiscordHelper
from database.database import Database
from database.media_action_status import MediaActionStatus
from database.media_status import MediaStatus
from database.media_type import MediaType
from database.user_group import UserGroup
from typing import Optional


class WebUtils:
    def __init__(self, database: Database, watch_updater: WatchUpdater, deleter: Deleter, status_updater: StatusUpdater, notifier: Notifier, discord: DiscordHelper):
        self.__logger = logging.getLogger(__name__)
        self.__database = database
        self.__watch_updater = watch_updater
        self.__deleter = deleter
        self.__status_updater = status_updater
        self.__notifier = notifier
        self.__discord = discord

        self.__lock = threading.RLock()

    def run_maintenance_full(self):
        with self.__lock:
            user_group_statuses = self.run_maintenance_updates()
            self.__deleter.delete()
            self.__notifier.notify_watchlist(user_group_statuses)
            self.__logger.info("Full maintenance done")

    def run_maintenance_updates(self, refresh_status: bool = True, refresh_watch: bool = True, user_id: Optional[int] = None) -> dict[UserGroup, UserGroupStatus]:
        with self.__lock:
            if refresh_status:
                self.__status_updater.update()

            user_group_statuses = {}
            if refresh_watch:
                user_group_statuses = self.__watch_updater.update(user_id)

            self.__logger.info("Updates maintenance done")
            return user_group_statuses

    def handle_season(self, overseerr_id: int, name: str, plex_user_id: int, season: Optional[int], media_type: MediaType):
        self.__logger.info(f"Handling requirement request for media with overseerr id {overseerr_id} (Season {season}) on plex id {plex_user_id}")
        medias = self.__database.media_get_by_overseerr_id(overseerr_id, season, media_type)
        user_groups = set(self.__database.user_group_get_with_plex_id(plex_user_id))

        if len(medias) == 0:
            self.__database.media_add(overseerr_id, name, season, media_type, MediaStatus.RELEASING, MediaActionStatus.TO_DELETE)
            medias = self.__database.media_get_by_overseerr_id(overseerr_id, season, media_type)
            for media in medias:
                self.__logger.info(f"Added media {media}")
                self.__discord.notify_media_added(media)
        else:
            for media in medias:
                self.__database.media_set_status(media.id, MediaStatus.RELEASING)
                self.__database.media_set_action_status(media.id, MediaActionStatus.TO_DELETE)

        self.__status_updater.update_medias(medias)

        if season:
            user_groups.update(self.__database.user_group_get_watching(overseerr_id, season - 1, media_type))

        for media in medias:
            for user_group in user_groups:
                self.__logger.info(f"Added media requirement for {user_group} on {media}")
                self.__database.media_requirement_add(media.id, user_group.id)
                self.__discord.notify_media_requirement_added(media, user_group)
                self.__notifier.notify_requirement_added(user_group, media)
