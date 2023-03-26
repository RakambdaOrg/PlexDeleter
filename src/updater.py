import logging
from api.overseerr_api import Overseerr
from api.tautulli_api import Tautulli
from database import Database
from notify.discord import Discord


class Updater:
    def __init__(self, database: Database, tautulli: Tautulli, overseerr: Overseerr, discord: Discord, completion_required: int = 90):
        self.__database = database
        self.__tautulli = tautulli
        self.__overseerr = overseerr
        self.__discord = discord
        self.__completion_required = completion_required
        self.__logger = logging.getLogger(__name__)

    def __has_user_watched(self, user_plex_id: int, metadata: dict, completion_required: int) -> bool:
        watched = any(map(lambda percent: percent >= completion_required,
                          map(lambda history: history['percent_complete'],
                              self.__tautulli.get_watch_history_for(user_plex_id, int(metadata['rating_key'])))))
        self.__logger.debug(f'User {user_plex_id} watched {metadata["media_index"]} - {metadata["title"]} : {watched}')
        return watched

    def __has_user_watched_all(self, user_plex_id: int, all_metadata: list[dict], completion_required: int) -> bool:
        return all(map(lambda metadata: self.__has_user_watched(user_plex_id, metadata, completion_required), all_metadata))

    def __has_group_watched_all(self, media_id, plex_ids) -> bool:
        self.__logger.debug(f'Updating media {media_id} for plex ids {plex_ids}')
        plex_id = self.__database.get_plex_id_for_finished_media(media_id)
        if not plex_id:
            self.__logger.warning(f'Media {media_id} not found on Plex or is not finished')
            return False

        all_metadata = self.__tautulli.get_all_metadata(str(plex_id))
        return any(self.__has_user_watched_all(plex_id, all_metadata, self.__completion_required) for plex_id in plex_ids)

    def __update_group(self, group_id: int) -> None:
        self.__logger.info(f'Updating group {group_id}')
        plex_ids = self.__database.get_plex_ids_in_group(group_id)
        media_ids = self.__database.get_waiting_media_ids_for_group(group_id)

        for media_id in media_ids:
            if self.__has_group_watched_all(media_id, plex_ids):
                self.__logger.info(f'Group {group_id} watched {media_id}')
                self.__database.mark_watched(media_id, group_id)
                self.__discord.notify_watched(media_id, group_id)

    def update_all_groups(self) -> None:
        self.__logger.info('Updating all groups')
        group_ids = self.__database.get_all_group_ids()
        for group_id in group_ids:
            self.__update_group(group_id)

    def __update_releasing(self, media_id: int, plex_id: int, overseerr_id: int) -> None:
        self.__logger.info(f'Updating releasing media {media_id}')
        metadata = self.__tautulli.get_metadata(str(plex_id))
        if not metadata or metadata['media_type'] != 'season':
            self.__logger.warning('Media is not a season or is not None plex_id anymore')
            return

        season_number = metadata['media_index']
        all_metadata = self.__tautulli.get_all_metadata(str(plex_id))
        last_episode = max(map(lambda data: int(data['media_index']), all_metadata))

        episode_count = self.__overseerr.get_tv_season_episode_count(overseerr_id, season_number)
        if episode_count is None:
            self.__logger.warning(f'Unknown max tv episode for media {media_id} and season {season_number}')
            return

        if episode_count <= last_episode:
            self.__logger.warning('Setting media as finished')
            self.__database.set_finished(media_id)
            self.__discord.notify_set_finished(media_id, season_number)

    def update_releasing(self) -> None:
        self.__logger.info('Updating releasing medias')
        self.__database.set_movies_finished()
        medias = self.__database.get_all_releasing_show()
        for media in medias:
            self.__update_releasing(media[0], media[1], media[2])
