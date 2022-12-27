from mediatype import MediaType
from requireduser import RequiredUser


class Media:
    def __init__(self, media_type: MediaType, rating_key: str, name: str, required_users: list[RequiredUser], completion_required: int = 90):
        self.__media_type = media_type
        self.__rating_key = rating_key
        self.__name = name
        self.__required_users = required_users
        self.__completion_required = min(completion_required, 100)

    def get_media_type(self) -> MediaType:
        return self.__media_type

    def get_rating_key(self) -> str:
        return self.__rating_key

    def get_required_users(self) -> list[RequiredUser]:
        return self.__required_users

    def get_completion_required(self):
        return self.__completion_required

    def __str__(self) -> str:
        return f'{self.__media_type.name} - {self.__rating_key} - {self.__name}'
