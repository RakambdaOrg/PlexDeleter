import logging
import operator
from functools import reduce
from typing import Optional

from api.tautulli.tautulli_api import TautulliApi


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

        root_rating_key = self.get_root_rating_key(rating_key)
        data = self.api.get_new_rating_keys(root_rating_key)
        seasons = data[0]["children"]
        if season_number not in seasons:
            return 0

        episodes = seasons[season_number]
        if len(episodes) == 0:
            return 0

        return max(episodes)

    def get_season_rating_key(self, rating_key: int, season_number: int) -> Optional[int]:
        if not season_number:
            return rating_key

        root_rating_key = self.get_root_rating_key(rating_key)
        data = self.api.get_new_rating_keys(root_rating_key)
        seasons = data[0]["children"]
        if season_number not in seasons:
            return None

        return seasons[season_number]["rating_key"]

    def has_user_watched_media(self, user_id: int, metadata: dict, completion_required: int) -> bool:
        watched = any(map(lambda percent: percent >= completion_required,
                          map(lambda history: history["percent_complete"],
                              self.api.get_watch_history_for_user_and_media(user_id, int(metadata["rating_key"]))["data"])))
        self.__logger.debug(f"{user_id} watched {metadata['media_index']} - {metadata['title']} : {watched}")
        return watched

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
