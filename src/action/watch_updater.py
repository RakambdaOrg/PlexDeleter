import logging

from src.database.media import Media
from src.database.media_status import MediaStatus
from src.database.user_group import UserGroup
from src.api.overseerr.overseerr_helper import OverseerrHelper
from src.database.database import Database
from src.api.discord.discord_helper import DiscordHelper
from src.api.tautulli.tautulli_helper import TautulliHelper
from src.database.user_person import UserPerson


class WatchUpdater:
    def __init__(self, database: Database, tautulli: TautulliHelper, overseerr: OverseerrHelper, discord: DiscordHelper, completion_required: int = 90):
        self.__logger = logging.getLogger(__name__)
        self.__database = database
        self.__tautulli = tautulli
        self.__overseerr = overseerr
        self.__discord = discord
        self.__completion_required = completion_required

    def __has_person_watched_all_medias(self, user_person: UserPerson, medias_metadata: list[dict]) -> bool:
        return all(map(lambda metadata: self.__tautulli.has_user_watched_media(user_person.plex_id, metadata, self.__completion_required), medias_metadata))

    def __has_persons_watched_media(self, media: Media, user_persons: list[UserPerson]) -> bool:
        self.__logger.debug(f"Querying watch status of {media} for persons {user_persons}")
        rating_key = self.__overseerr.get_plex_rating_key(media.overseerr_id, media.type)
        if not rating_key:
            self.__logger.warning(f"Could not find media rating keys for {media}, not available?")
            return False

        season_rating_key = self.__tautulli.get_season_rating_key(rating_key, media.season_number)
        if not season_rating_key:
            self.__logger.warning(f"Could not find season {media.season_number}media rating keys for {media}, not available?")
            return False

        all_metadata = self.__tautulli.get_movie_and_all_episodes_metadata(season_rating_key)
        return any(self.__has_person_watched_all_medias(user_person, all_metadata) for user_person in user_persons)

    def __update_group(self, user_group: UserGroup) -> None:
        self.__logger.info(f"Updating {user_group}")
        user_persons = self.__database.user_person_get_all_in_group(user_group.id)
        medias = self.__database.media_get_waiting_for_group(user_group.id)

        for media in medias:
            if media.status != MediaStatus.FINISHED:
                continue
            if self.__has_persons_watched_media(media, user_persons):
                self.__logger.info(f"{user_group} watched {media}")
                self.__database.media_requirement_set_watched(media.id, user_group.id)
                self.__discord.notify_watched(media, user_group)

    def update(self) -> None:
        self.__logger.info("Updating watch statuses for all groups")
        user_groups = self.__database.user_group_get_all()
        for user_group in user_groups:
            self.__update_group(user_group)
