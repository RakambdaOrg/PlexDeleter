import logging

from action.status.user_group_status import UserGroupStatus
from action.status.user_media_status import UserMediaStatus
from api.tautulli.data.user_group_watch_status import UserGroupWatchStatus
from database.media import Media
from database.media_status import MediaStatus
from database.user_group import UserGroup
from api.overseerr.overseerr_helper import OverseerrHelper
from database.database import Database
from api.discord.discord_helper import DiscordHelper
from api.tautulli.tautulli_helper import TautulliHelper
from database.user_person import UserPerson


class WatchUpdater:
    def __init__(self, database: Database, tautulli: TautulliHelper, overseerr: OverseerrHelper, discord: DiscordHelper, completion_required: int = 85):
        self.__logger = logging.getLogger(__name__)
        self.__database = database
        self.__tautulli = tautulli
        self.__overseerr = overseerr
        self.__discord = discord
        self.__completion_required = completion_required

    def __has_persons_watched_media(self, media: Media, user_persons: list[UserPerson], user_group_watch_status: UserGroupWatchStatus) -> UserMediaStatus:
        self.__logger.debug(f"Querying watch status of {media} for persons {user_persons}")
        user_media_status = UserMediaStatus()
        rating_key = self.__overseerr.get_plex_rating_key(media.overseerr_id, media.type)
        if not rating_key:
            self.__logger.warning(f"Could not find media rating keys for {media}, not available?")
            user_media_status.add_unknown_index()
            self.__discord.notify_cannot_update_watch(media)
            return user_media_status

        element_rating_key = self.__tautulli.get_season_episode_rating_key(rating_key, media.season_number)
        media_element_rating_keys = []

        if not media.season_number:
            media_element_rating_keys.append(element_rating_key)
        else:
            season_element_rating_key = element_rating_key.get_child(media.season_number)
            if season_element_rating_key:
                media_element_rating_keys.extend(season_element_rating_key.children.values())

        if len(media_element_rating_keys) == 0:
            self.__logger.warning(f"Could not find season {media.season_number}media rating keys for {media}, not available?")
            user_media_status.add_unknown_index()
            self.__discord.notify_cannot_update_watch(media)
            return user_media_status

        if not user_group_watch_status.rating_key_searched(rating_key):
            user_group_watch_status.merge(self.__tautulli.watched_status_for_media(media.type, rating_key))
        for media_element_rating_key in media_element_rating_keys:
            watched = False
            for user_person in user_persons:
                media_plex_id = media_element_rating_key.rating_key
                watched |= self.__completion_required <= user_group_watch_status.get_user_watch_status(user_person.plex_id).get_watch_percentage(media_plex_id)
            if not watched:
                user_media_status.add_index(media_element_rating_key.index)
        return user_media_status

    def __update_group(self, user_group: UserGroup, user_group_watch_status: UserGroupWatchStatus) -> UserGroupStatus:
        self.__logger.info(f"Updating {user_group}")
        user_persons = self.__database.user_person_get_all_in_group(user_group.id)
        medias = self.__database.media_get_waiting_for_user_group(user_group.id)

        user_group_status = UserGroupStatus()
        for media in medias:
            if media.status != MediaStatus.FINISHED:
                continue
            user_media_status = self.__has_persons_watched_media(media, user_persons, user_group_watch_status)
            user_group_status.add(media, user_media_status)
            if user_media_status.is_all_watched():
                self.__logger.info(f"{user_group} watched {media}")
                self.__database.media_requirement_set_watched(media.id, user_group.id)
                self.__discord.notify_watched(media, user_group)
            else:
                status = f' | Waiting EPs {user_media_status.get_all_str()}' if media.type == MediaType.SHOW and user_media_status and not user_media_status.is_all_watched() else ''
                self.__logger.info(f"{user_group} did not watch {media}{status}")
        return user_group_status

    def update(self) -> dict[UserGroup, UserGroupStatus]:
        self.__logger.info("Updating watch statuses for all groups")
        user_groups = self.__database.user_group_get_all()

        user_group_watch_status = UserGroupWatchStatus()
        missing_watched = {}
        for user_group in user_groups:
            missing_watched[user_group] = self.__update_group(user_group, user_group_watch_status)

        return missing_watched
