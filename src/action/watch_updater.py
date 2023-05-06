import logging

from action.status.user_group_status import UserGroupStatus
from action.status.user_media_status import UserMediaStatus
from database.media import Media
from database.media_status import MediaStatus
from database.user_group import UserGroup
from api.overseerr.overseerr_helper import OverseerrHelper
from database.database import Database
from api.discord.discord_helper import DiscordHelper
from api.tautulli.tautulli_helper import TautulliHelper
from database.user_person import UserPerson


class WatchUpdater:
    def __init__(self, database: Database, tautulli: TautulliHelper, overseerr: OverseerrHelper, discord: DiscordHelper, completion_required: int = 90):
        self.__logger = logging.getLogger(__name__)
        self.__database = database
        self.__tautulli = tautulli
        self.__overseerr = overseerr
        self.__discord = discord
        self.__completion_required = completion_required

    def __has_person_watched_all_medias(self, user_person: UserPerson, medias_metadata: list[dict]) -> list[dict]:
        return list(filter(lambda metadata: self.__tautulli.has_user_watched_media(user_person.plex_id, metadata, self.__completion_required) is False, medias_metadata))

    def __has_persons_watched_media(self, media: Media, user_persons: list[UserPerson]) -> UserMediaStatus:
        self.__logger.debug(f"Querying watch status of {media} for persons {user_persons}")
        user_media_status = UserMediaStatus()
        rating_key = self.__overseerr.get_plex_rating_key(media.overseerr_id, media.type)
        if not rating_key:
            self.__logger.warning(f"Could not find media rating keys for {media}, not available?")
            user_media_status.add_unknown_index()
            self.__discord.notify_cannot_update_watch(media)
            return user_media_status

        season_rating_key = self.__tautulli.get_season_rating_key(rating_key, media.season_number)
        if not season_rating_key:
            self.__logger.warning(f"Could not find season {media.season_number}media rating keys for {media}, not available?")
            user_media_status.add_unknown_index()
            self.__discord.notify_cannot_update_watch(media)
            return user_media_status

        all_metadata = self.__tautulli.get_movie_and_all_episodes_metadata(season_rating_key)
        for metadata in all_metadata:
            watched = False
            for user_person in user_persons:
                watched |= self.__tautulli.has_user_watched_media(user_person.plex_id, metadata, self.__completion_required)
            if not watched:
                user_media_status.add_index(int(metadata['media_index']) if metadata['media_index'] else 0)
        return user_media_status

    def __update_group(self, user_group: UserGroup) -> UserGroupStatus:
        self.__logger.info(f"Updating {user_group}")
        user_persons = self.__database.user_person_get_all_in_group(user_group.id)
        medias = self.__database.media_get_waiting_for_group(user_group.id)

        user_group_status = UserGroupStatus()
        for media in medias:
            if media.status != MediaStatus.FINISHED:
                continue
            user_media_status = self.__has_persons_watched_media(media, user_persons)
            user_group_status.add(media, user_media_status)
            if user_media_status.is_all_watched():
                self.__logger.info(f"{user_group} watched {media}")
                self.__database.media_requirement_set_watched(media.id, user_group.id)
                self.__discord.notify_watched(media, user_group)
        return user_group_status

    def update(self) -> dict[UserGroup, UserGroupStatus]:
        self.__logger.info("Updating watch statuses for all groups")
        user_groups = self.__database.user_group_get_all()

        missing_watched = {}
        for user_group in user_groups:
            missing_watched[user_group] = self.__update_group(user_group)

        return missing_watched
