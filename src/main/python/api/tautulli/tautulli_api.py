from tautulli import RawAPI


class TautulliApi:
    def __init__(self, url, key):
        self.__api = RawAPI(base_url=url, api_key=key)
        self.__max_length = 10000

    def get_metadata(self, rating_key: int) -> dict:
        return self.__check_response(self.__api.get_metadata(rating_key=str(rating_key)))

    def get_library_media_info(self, rating_key: int) -> dict:
        return self.__check_response(self.__api.get_library_media_info(rating_key=str(rating_key), length=self.__max_length, refresh=True))

    def get_watch_history_for_user_and_media(self, user_id: int, rating_key: int) -> dict:
        return self.__check_response(self.__api.get_history(user_id=user_id, rating_key=rating_key, length=self.__max_length))

    def get_watch_history_for_rating_key(self, rating_key: int) -> dict:
        return self.__check_response(self.__api.get_history(rating_key=rating_key, length=self.__max_length))

    def get_watch_history_for_grandparent_rating_key(self, grandparent_rating_key: int) -> dict:
        return self.__check_response(self.__api.get_history(grandparent_rating_key=grandparent_rating_key, length=self.__max_length))

    def get_new_rating_keys(self, rating_key: int, media_type: str) -> dict:
        return self.__check_response(self.__api.get_new_rating_keys(rating_key=str(rating_key), media_type=media_type))

    @staticmethod
    def __check_response(response: dict) -> dict:
        return response
