import operator
from functools import reduce

from tautulli import RawAPI

from src.mediatype import MediaType


class Api:
    def __init__(self, url, key):
        self.__api = RawAPI(base_url=url, api_key=key)
        self.__section_ids = {
            MediaType.MOVIE.value: '21',
            MediaType.SHOW.value: '22'
        }

    def get_metadata(self, rating_key: str) -> list[dict]:
        data = self.__api.get_metadata(rating_key=rating_key)
        if not data:
            return []

        media_type = data['media_type']
        if media_type == MediaType.MOVIE.value or media_type == MediaType.EPISODE.value:
            return [data]

        if media_type == MediaType.SHOW.value or media_type == MediaType.SEASON.value:
            return list(reduce(operator.iconcat,
                               map(lambda child_rating_key: self.get_metadata(child_rating_key), self.get_child_rating_keys(rating_key)),
                               []))

        raise RuntimeError(f'Unsupported media type {media_type}')

    def get_child_rating_keys(self, rating_key: str) -> list[str]:
        response = self.__api.get_library_media_info(rating_key=rating_key, length=1000)
        return list(map(lambda data: data['rating_key'], response['data'])) \
            if 'data' in response \
            else []

    def get_watch_history_for(self, user_id: int, rating_key: int) -> list[dict]:
        response = self.__api.get_history(user_id=user_id, rating_key=rating_key, length=1000)
        return response['data'] \
            if 'data' in response \
            else []
