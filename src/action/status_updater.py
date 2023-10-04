import logging
from datetime import datetime
from typing import Optional

from api.discord.discord_helper import DiscordHelper
from api.overseerr.overseerr_helper import OverseerrHelper
from api.radarr.radarr_helper import RadarrHelper
from api.sonarr.sonarr_helper import SonarrHelper
from api.tautulli.tautulli_helper import TautulliHelper
from database.database import Database
from database.media import Media
from database.media_status import MediaStatus
from database.media_type import MediaType


class StatusUpdater:
    def __init__(self, database: Database, tautulli: TautulliHelper, overseerr: OverseerrHelper, discord: DiscordHelper, radarr: RadarrHelper, sonarr: SonarrHelper):
        self.__logger = logging.getLogger(__name__)
        self.__database = database
        self.__tautulli = tautulli
        self.__overseerr = overseerr
        self.__discord = discord
        self.__radarr = radarr
        self.__sonarr = sonarr

    def update(self) -> None:
        self.__logger.info("Updating media statuses")
        self.update_medias(self.__database.media_get_all_releasing())

    def update_medias(self, medias: list[Media]) -> None:
        self.__logger.info(f"Updating {len(medias)} medias")
        for media in medias:
            self.update_media(media)

    def update_media(self, media: Media) -> None:
        self.__logger.info(f"Updating media status {media}")
        if media.type == MediaType.MOVIE:
            self.__update_movie(media)
        else:
            self.__update_series(media)

    def __update_movie(self, media: Media) -> None:
        media_details = self.__overseerr.get_media_details(media.overseerr_id, media.type)
        if media_details.rating_key:
            self.__mark_finished(media, 1, 1)
            return

        if media_details.tmdb_id:
            movie_has_file = self.__radarr.has_file(media_details.tmdb_id)
            if movie_has_file:
                self.__mark_finished(media, 1, 1)
            return

        self.__logger.warning(f"Skipped updating {media}, no rating key or Radarr data found for given media")
        self.__discord.notify_cannot_update(media)

    def __update_series(self, media: Media) -> None:
        season_details = self.__overseerr.get_tv_season_details(media.overseerr_id, media.season_number)

        element_count = None
        total_element_count = season_details.episode_count
        self.__logger.info(f"Got total element count of {total_element_count} from Overseerr")

        result = self.__get_episode_count_from_tautulli(media)
        if result:
            self.__logger.info(f"Got element count of {element_count} from Tautulli")
            element_count = result

        result = self.__get_episode_count_from_radarr(media)
        if result:
            if result[0]:
                self.__logger.info(f"Got element count of {result[0]} from Radarr")
                if not element_count or result[0] > element_count:
                    element_count = result[0]
            if result[1]:
                self.__logger.info(f"Got total element count of {result[1]} from Radarr")
                if not total_element_count or result[1] > total_element_count:
                    total_element_count = result[1]

        if not element_count or not total_element_count:
            self.__logger.warning(f"Skipped updating {media}, no rating key or Sonarr data found for given media")
            if season_details.last_air_date and season_details.last_air_date < datetime.now():
                self.__discord.notify_cannot_update(media)
            return

        self.__database.media_set_element_count(media.id, total_element_count)
        if element_count >= total_element_count:
            self.__mark_finished(media, element_count, total_element_count)

    def __get_episode_count_from_tautulli(self, media: Media) -> Optional[int]:
        media_details = self.__overseerr.get_media_details(media.overseerr_id, media.type)
        if not media_details.rating_key:
            return

        episode_count = self.__tautulli.get_episode_count_in_season(media_details.rating_key, media.season_number)
        return episode_count

    def __get_episode_count_from_radarr(self, media: Media) -> Optional[tuple[int, int]]:
        media_details = self.__overseerr.get_media_details(media.overseerr_id, media.type)
        if not media_details.tvdb_id:
            return

        return self.__sonarr.get_tv_season_episode(media_details.tvdb_id, media.season_number)

    def __mark_finished(self, media: Media, element_count: int, total_element_count: int) -> None:
        self.__logger.info(f'Setting media as finished {element_count}/{total_element_count}')
        self.__database.media_set_status(media.id, MediaStatus.FINISHED)
        self.__discord.notify_set_finished(media, element_count, total_element_count)
