import logging

from api.overseerr.overseerr_helper import OverseerrHelper
from api.radarr.radarr_helper import RadarrHelper
from api.sonarr.sonarr_helper import SonarrHelper
from database.database import Database
from database.media_type import MediaType
from database.media import Media
from api.discord.discord_helper import DiscordHelper
from api.tautulli.tautulli_helper import TautulliHelper


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
        medias = self.__database.media_get_all_releasing()
        for media in medias:
            self.__logger.info(f"Updating releasing media {media}")
            if media.type == MediaType.MOVIE:
                self.__update_movie(media)
            else:
                self.__update_series(media)

    def __update_movie(self, media: Media) -> None:
        rating_key = self.__overseerr.get_plex_rating_key(media.overseerr_id, media.type)
        if rating_key:
            self.__mark_finished(media)
            return

        tmdb_id = self.__overseerr.get_tmdb_id(media.overseerr_id, media.type)
        if tmdb_id:
            movie_has_file = self.__radarr.has_file(tmdb_id)
            if movie_has_file:
                self.__mark_finished(media)
            return

        self.__logger.warning(f"Skipped updating {media}, no rating key or Radarr data found for given media")
        self.__discord.notify_cannot_update(media)

    def __update_series(self, media: Media) -> None:
        rating_key = self.__overseerr.get_plex_rating_key(media.overseerr_id, media.type)
        if rating_key:
            last_available_episode = self.__tautulli.get_last_episode_number_in_season(rating_key, media.season_number)
            season_episode_count = self.__overseerr.get_tv_season_episode_count(media.overseerr_id, media.season_number)

            if season_episode_count:
                if season_episode_count <= last_available_episode:
                    self.__mark_finished(media)
                return

        tvdb_id = self.__overseerr.get_tvdb_id(media.overseerr_id, media.type)
        if tvdb_id:
            season_episode_percentage = self.__sonarr.get_tv_season_episode_percentage(tvdb_id, media.season_number)
            if season_episode_percentage:
                if season_episode_percentage == 100:
                    self.__mark_finished(media)
                return

        self.__logger.warning(f"Skipped updating {media}, no rating key or Sonarr data found for given media")
        self.__discord.notify_cannot_update(media)

    def __mark_finished(self, media: Media) -> None:
        self.__logger.info('Setting media as finished')
        self.__database.media_set_finished(media.id)
        self.__discord.notify_set_finished(media)
