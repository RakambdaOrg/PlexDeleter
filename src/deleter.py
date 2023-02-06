import logging
import operator
from functools import reduce
from pathlib import Path
from api import Api
from database import Database


class Deleter:
    def __init__(self, remote_path: str, local_path: str, dry_run: bool, database: Database, api: Api):
        self.__remote_path = remote_path
        self.__local_path = local_path
        self.__dry_run = dry_run
        self.__database = database
        self.__api = api
        self.__logger = logging.getLogger(__name__)

    def delete_all(self, media_ids: list[int]):
        plex_ids = filter(lambda x: x is not None, (self.__database.get_plex_id_for_finished_media(media_id) for media_id in media_ids))
        all_metadata = reduce(operator.iconcat, (self.__api.get_metadata(str(plex_id)) for plex_id in plex_ids), [])

        files = set(map(lambda part: part['file'],
                        reduce(operator.iconcat,
                               map(lambda media_info: media_info['parts'],
                                   reduce(operator.iconcat,
                                          map(lambda metadata: metadata['media_info'], all_metadata),
                                          [])),
                               [])))
        local_files = set([Path(file.replace(self.__remote_path, self.__local_path, 1)) for file in files])
        self.__delete_recursive(local_files)

    def __delete_recursive(self, files: set[Path]):
        all_parents = set()
        while len(files) > 0:
            (parents, companion_files) = self.__delete_files(files)
            files = companion_files
            all_parents += parents
        if len(all_parents) > 0:
            self.__delete_recursive(all_parents)

    def __delete_files(self, files: set[Path]) -> (set[Path], set[Path]):
        parents = set()
        companion_files = set()

        for file in files:
            if file.is_file():
                self.__logger.info(f'Deleting file {file}')
                parents.add(file.parent)
                if not self.__dry_run:
                    file.unlink()
                companion_files += self.__get_companion_files(file)
            if file.is_dir():
                self.__logger.info(f'Deleting folder {file}')
                children = list(file.glob('*'))
                if len(children) > 0:
                    self.__logger.info(f'Folder not empty')
                    continue
                parents.add(file.parent)
                if not self.__dry_run:
                    file.rmdir()
        return parents, companion_files

    @staticmethod
    def __get_companion_files(file: Path) -> set[Path]:
        return set(file.parent.glob(f'{file.stem}.*.srt'))

