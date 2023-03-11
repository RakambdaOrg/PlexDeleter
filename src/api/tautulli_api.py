import operator
from functools import reduce
from tautulli import RawAPI


class Tautulli:
    def __init__(self, url, key):
        self.__api = RawAPI(base_url=url, api_key=key)

    def get_metadata(self, rating_key: str) -> dict | None:
        return self.__api.get_metadata(rating_key=rating_key)

    def get_all_metadata(self, rating_key: str) -> list[dict]:
        data = self.get_metadata(rating_key=rating_key)
        if not data:
            return []

        media_type = data['media_type']
        if media_type == 'movie' or media_type == 'episode':
            return [data]

        if media_type == 'show' or media_type == 'season':
            return list(reduce(operator.iconcat,
                               map(lambda child_rating_key: self.get_all_metadata(child_rating_key), self.get_child_rating_keys(rating_key)),
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
