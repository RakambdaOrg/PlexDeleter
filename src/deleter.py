import logging
import operator
import os
from functools import reduce


class Deleter:
    def __init__(self, remote_path: str, local_path: str, dry_run: bool):
        self.__remote_path = remote_path
        self.__local_path = local_path
        self.__dry_run = dry_run
        self.__logger = logging.getLogger(__name__)

    def delete_all(self, all_metadata: list[dict]):
        files = set(map(lambda part: part['file'],
                        reduce(operator.iconcat,
                               map(lambda media_info: media_info['parts'],
                                   reduce(operator.iconcat,
                                          map(lambda metadata: metadata['media_info'], all_metadata),
                                          [])),
                               [])))

        for file in files:
            local_file = file.replace(self.__remote_path, self.__local_path, 1)
            self.__logger.info(f'Deleting {file} ({local_file})')
            if not self.__dry_run:
                os.remove(local_file)
