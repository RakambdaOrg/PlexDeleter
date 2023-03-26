from tautulli import RawAPI


class TautulliApi:
    def __init__(self, url, key):
        self.__api = RawAPI(base_url=url, api_key=key)

    def get_metadata(self, rating_key: int) -> dict:
        return self.__check_response(self.__api.get_metadata(rating_key=str(rating_key)))

    def get_library_media_info(self, rating_key: int) -> dict:
        return self.__check_response(self.__api.get_library_media_info(rating_key=str(rating_key), length=1000, refresh=True))

    def get_watch_history_for_user_and_media(self, user_id: int, rating_key: int) -> dict:
        return self.__check_response(self.__api.get_history(user_id=user_id, rating_key=rating_key, length=1000))

    def get_new_rating_keys(self, rating_key: int) -> dict:
        return self.__check_response(self.__api.get_new_rating_keys(rating_key=str(rating_key)))

    @staticmethod
    def __check_response(response: dict) -> dict:
        if response["result"] != "success":
            raise RuntimeError("Tautulli API response is not success")
        return response["data"]
