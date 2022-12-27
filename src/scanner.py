import logging
import operator
from datetime import datetime
from functools import reduce

from api import Api
from media import Media
from requireduser import RequiredUser


class Scanner:
    def __init__(self, api: Api, minimum_aged: int = 24 * 60 * 60):
        self.__api = api
        self.__minimum_aged = minimum_aged
        self.__logger = logging.getLogger(__name__)

    def __has_user_watched(self, required_user: RequiredUser, metadata: dict, completion_required: int) -> bool:
        watched = any(map(lambda percent: percent >= completion_required,
                          map(lambda history: history['percent_complete'],
                              reduce(operator.iconcat,
                                     map(lambda user_id: self.__api.get_watch_history_for(required_user.value, int(metadata['rating_key'])),
                                         required_user.value),
                                     []))))
        self.__logger.debug(f'User {required_user.name} watched {metadata["media_index"]} - {metadata["title"]} : {watched}')
        return watched

    def __has_user_watched_all(self, required_user: RequiredUser, all_metadata: list[dict], completion_required: int) -> bool:
        return all(map(lambda metadata: self.__has_user_watched(required_user, metadata, completion_required), all_metadata))

    @staticmethod
    def __get_elapsed_since_added(metadata: dict) -> float:
        timestamp = datetime.fromtimestamp(int(metadata['added_at']))
        return (datetime.now() - timestamp).total_seconds()

    def process_media(self, media: Media) -> list[dict] | None:
        self.__logger.debug(f'Processing media {media}')
        all_metadata = self.__api.get_metadata(media.get_rating_key())
        if len(all_metadata) <= 0:
            print(f'{media} => No metadata found mathing request ; skipping')
            return None

        self.__logger.debug(f'Found {len(all_metadata)} parts to be watched')
        not_aged_enough = list(filter(lambda metadata: self.__get_elapsed_since_added(metadata) < self.__minimum_aged, all_metadata))

        if len(not_aged_enough) > 0:
            names = list(map(lambda metadata: metadata['title'], not_aged_enough))
            self.__logger.info(f'{media} => Not all elements are aged enough ; skipping ({names})')
            return None

        missing_watchers = list(filter(lambda user: not self.__has_user_watched_all(user, all_metadata, media.get_completion_required()),
                                       media.get_required_users()))

        if len(missing_watchers) > 0:
            names = list(map(lambda user: user.name, missing_watchers))
            self.__logger.info(f'{media} => Not everyone watched ; skipping ({names})')
            return None

        self.__logger.info(f'{media} => Everyone watched ; deleting')
        return all_metadata
