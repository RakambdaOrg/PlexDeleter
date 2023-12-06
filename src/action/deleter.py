import logging
from datetime import datetime, timedelta
from pathlib import Path

import humanize

from api.discord.discord_helper import DiscordHelper
from api.overseerr.overseerr_helper import OverseerrHelper
from api.tautulli.tautulli_helper import TautulliHelper
from database.database import Database
from database.media import Media
from database.media_action_status import MediaActionStatus


class Deleter:
    def __init__(self, remote_path: str, local_path: str, dry_run: bool, database: Database, tautulli: TautulliHelper, overseerr: OverseerrHelper, discord: DiscordHelper, min_days: int):
        self.__remote_path = remote_path
        self.__local_path = local_path
        self.__dry_run = dry_run
        self.__database = database
        self.__tautulli = tautulli
        self.__overseerr = overseerr
        self.__discord = discord
        self.__min_days = min_days
        self.__logger = logging.getLogger(__name__)

    def delete(self) -> None:
        medias = self.__database.media_get_fully_watched_to_delete()
        if len(medias) == 0:
            return

        deleted_size = 0
        for media in medias:
            try:
                deleted_size += self.__delete_media(media)
            except IOError as error:
                self.__logger.error(f"Failed to delete media {media}", exc_info=error)
        self.__logger.error(f"Total deleted size : {humanize.naturalsize(deleted_size)}")
        self.__discord.notify_total_deleted(deleted_size)

    def __delete_media(self, media: Media) -> int:
        metadata = []
        media_details = self.__overseerr.get_media_details(media.overseerr_id, media.type)
        element_rating_key = self.__tautulli.get_season_episode_rating_key(media_details.rating_key, media.season_number)

        season_rating_key = None
        if element_rating_key:
            if not media.season_number:
                season_rating_key = element_rating_key.rating_key
            else:
                season_element_rating_key = element_rating_key.get_child(media.season_number)
                if season_element_rating_key:
                    season_rating_key = season_element_rating_key.rating_key

        if season_rating_key:
            sub_metadata = self.__tautulli.get_movie_and_all_episodes_metadata(season_rating_key)
            timestamp = max(map(lambda meta: int(meta["added_at"] or "0"), sub_metadata), default=0)
            max_date = datetime.fromtimestamp(timestamp)
            if datetime.now() - max_date >= timedelta(days=self.__min_days):
                metadata.extend(sub_metadata)
            else:
                self.__logger.info(f"Skipped {media} because most recent file is from {max_date} which is not older than {self.__min_days} days")
                return 0
        else:
            self.__logger.warning(f"Could not find metadata & files for {media}")
            self.__discord.notify_cannot_delete(media)
            return 0

        files = set()
        for m in metadata:
            medias_info = m["media_info"] or []
            for media_info in medias_info:
                parts = media_info["parts"] or []
                for parts in parts:
                    remote_file = parts["file"]
                    local_file = Path(remote_file.replace(self.__remote_path, self.__local_path, 1))
                    files.add(local_file)

        deleted_size = self.__delete_recursive(files)
        if not self.__dry_run:
            self.__database.media_set_action_status(media.id, MediaActionStatus.DELETED)
            self.__discord.notify_media_deleted(media, deleted_size)

        return deleted_size

    def __delete_recursive(self, files: set[Path]) -> int:
        all_parents = set()
        deleted_size = 0
        while len(files) > 0:
            (parents, companion_files, size) = self.__delete_files(files)
            files = companion_files
            all_parents.update(parents)
            deleted_size += size
        if len(all_parents) > 0:
            deleted_size += self.__delete_recursive(all_parents)
        return deleted_size

    def __delete_files(self, files: set[Path]) -> (set[Path], set[Path], int):
        parents = set()
        companion_files = set()
        deleted_size = 0

        for file in sorted(files):
            if file.is_file():
                self.__logger.info(f"Deleting file {file}")
                parents.add(file.parent)
                if not self.__dry_run:
                    size = file.stat().st_size
                    file.unlink()
                    deleted_size += size
                    self.__discord.notify_file_deleted(file.relative_to(Path(self.__local_path)), size)
                companion_files.update(self.__get_companion_files(file))
            if file.is_dir():
                self.__logger.info(f"Deleting folder {file}")
                children = list(file.glob('*'))
                if len(children) > 0:
                    self.__logger.info(f"Folder not empty")
                    continue
                parents.add(file.parent)
                if not self.__dry_run:
                    file.rmdir()
                    self.__discord.notify_folder_deleted(file.relative_to(Path(self.__local_path)))
        return parents, companion_files, deleted_size

    @staticmethod
    def __get_companion_files(file: Path) -> set[Path]:
        return set(file.parent.glob(f"*.srt")) \
            .union(set(file.parent.glob(f"*.smi"))) \
            .union(set(file.parent.glob(f"*.xml"))) \
            .union(set(file.parent.glob(f"*.nfo"))) \
            .union(set(file.parent.glob(f"*.metathumb"))) \
            .union(set(file.parent.glob(f"*.png"))) \
            .union(set(file.parent.glob(f"*.jpg"))) \
            .union(set(file.parent.glob(f"*.jpeg"))) \
            .union(set(file.parent.glob(f"@eaDir/*"))) \
            .union(set(file.parent.glob(f"Plex Versions/**")))
