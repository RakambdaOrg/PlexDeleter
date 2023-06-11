import array
import logging
import operator
from functools import reduce
from typing import Optional

import pydash

from api.tautulli.data.user_group_watch_status import UserGroupWatchStatus
from api.tautulli.data.user_watch_status import UserWatchStatus
from api.tautulli.data.watch_status import WatchStatus
from api.tautulli.tautulli_api import TautulliApi
from database.media_type import MediaType


class TautulliHelper:
    def __init__(self, tautulli: TautulliApi):
        self.__logger = logging.getLogger(__name__)
        self.api = tautulli

    def get_root_rating_key(self, rating_key: int) -> int:
        data = self.api.get_metadata(rating_key)
        if not data:
            return rating_key
        if data["grandparent_rating_key"]:
            return int(data["grandparent_rating_key"])
        if data["parent_rating_key"]:
            return int(data["parent_rating_key"])
        return rating_key

    def get_last_episode_number_in_season(self, rating_key: int, season_number: int) -> int:
        if not season_number:
            return 0

        data = self.__get_all_keys_for(rating_key)
        episodes = pydash.get(data, f"0.children.{season_number}.children", None)
        if not episodes or len(episodes) == 0:
            return 0

        return max(map(lambda a: int(a), episodes))

    def get_season_rating_key(self, rating_key: int, season_number: int) -> Optional[int]:
        if not season_number:
            return rating_key

        data = self.__get_all_keys_for(rating_key)
        return pydash.get(data, f"0.children.{season_number}.rating_key", None)

    def get_movie_and_all_episodes_metadata(self, rating_key: int) -> list[dict]:
        data = self.api.get_metadata(rating_key=rating_key)
        if not data:
            return []

        media_type = data["media_type"]
        if media_type == "movie" or media_type == "episode":
            return [data]

        if media_type == "show" or media_type == "season":
            return list(reduce(operator.iconcat,
                               map(lambda child_rating_key: self.get_movie_and_all_episodes_metadata(child_rating_key), self.__get_child_rating_keys(rating_key)),
                               []))

        raise RuntimeError(f"Unsupported media type {media_type}")

    def __get_child_rating_keys(self, rating_key: int) -> list[str]:
        response = self.api.get_library_media_info(rating_key)
        return list(map(lambda data: data["rating_key"], response["data"]))

    def __get_all_keys_for(self, rating_key: int) -> array:
        root_rating_key = self.get_root_rating_key(rating_key)
        metadata = self.api.get_metadata(rating_key)
        if "media_type" not in metadata:
            return {}
        return self.api.get_new_rating_keys(root_rating_key, metadata["media_type"])

    def watched_status_for_media(self, media_type: MediaType, rating_key: int) -> UserGroupWatchStatus:
        data = None
        if media_type == MediaType.MOVIE:
            data = self.api.get_watch_history_for_rating_key(rating_key)["data"]
        elif media_type == MediaType.SHOW:
            data = self.api.get_watch_history_for_grandparent_rating_key(rating_key)["data"]

        user_group_watch_status = UserGroupWatchStatus()
        if data:
            for media_data in data:
                user_id = media_data['user_id']
                plex_id = media_data['rating_key']
                watch_status = WatchStatus(int(pydash.get(data, f"parent_media_index", '0')), int(pydash.get(data, f"media_index", '0')), media_data["percent_complete"])
                user_group_watch_status.add_watch_status(user_id, plex_id, watch_status)

        return user_group_watch_status
