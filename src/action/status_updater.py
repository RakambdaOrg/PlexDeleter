import logging

from api.overseerr.overseerr_helper import OverseerrHelper
from database.database import Database
from database.media_type import MediaType
from database.media import Media
from api.discord.discord_helper import DiscordHelper
from api.tautulli.tautulli_helper import TautulliHelper


class StatusUpdater:
    def __init__(self, database: Database, tautulli: TautulliHelper, overseerr: OverseerrHelper, discord: DiscordHelper):
        self.__logger = logging.getLogger(__name__)
        self.__database = database
        self.__tautulli = tautulli
        self.__overseerr = overseerr
        self.__discord = discord

    def update(self) -> None:
        self.__logger.info("Updating media statuses")
        medias = self.__database.media_get_all_releasing()
        for media in medias:
            self.__update_media(media)

    def __update_media(self, media: Media) -> None:
        self.__logger.info(f"Updating releasing media {media}")

        rating_key = self.__overseerr.get_plex_rating_key(media.overseerr_id, media.type)
        if not rating_key:
            self.__logger.warning(f"Skipped updating {media}, no rating key found for given media")
            self.__discord.notify_cannot_update(media)
            return

        if media.type == MediaType.MOVIE:
            self.__mark_finished(media)
            return

        last_available_episode = self.__tautulli.get_last_episode_number_in_season(rating_key, media.season_number)
        season_episode_count = self.__overseerr.get_tv_season_episode_count(media.overseerr_id, media.season_number)

        if season_episode_count is None:
            self.__logger.warning(f'Unknown least media episode')
            return

        if season_episode_count <= last_available_episode:
            self.__mark_finished(media)

    def __mark_finished(self, media: Media) -> None:
        self.__logger.info('Setting media as finished')
        self.__database.media_set_finished(media.id)
        self.__discord.notify_set_finished(media)
