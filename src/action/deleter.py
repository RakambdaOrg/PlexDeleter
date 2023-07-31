import logging
from datetime import datetime, timezone, timedelta
from pathlib import Path
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
        for media in medias:
            self.__delete_media(media)

    def __delete_media(self, media: Media) -> None:
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
                return
        else:
            self.__logger.warning(f"Could not find metadata & files for {media}")
            self.__discord.notify_cannot_delete(media)
            return

        files = set()
        for m in metadata:
            medias_info = m["media_info"] or []
            for media_info in medias_info:
                parts = media_info["parts"] or []
                for parts in parts:
                    remote_file = parts["file"]
                    local_file = Path(remote_file.replace(self.__remote_path, self.__local_path, 1))
                    files.add(local_file)

        self.__delete_recursive(files)
        if not self.__dry_run:
            self.__database.media_set_action_status(media.id, MediaActionStatus.DELETED)
            self.__discord.notify_media_deleted(media)

    def __delete_recursive(self, files: set[Path]) -> None:
        all_parents = set()
        while len(files) > 0:
            (parents, companion_files) = self.__delete_files(files)
            files = companion_files
            all_parents.update(parents)
        if len(all_parents) > 0:
            self.__delete_recursive(all_parents)

    def __delete_files(self, files: set[Path]) -> (set[Path], set[Path]):
        parents = set()
        companion_files = set()

        for file in sorted(files):
            if file.is_file():
                self.__logger.info(f"Deleting file {file}")
                parents.add(file.parent)
                if not self.__dry_run:
                    file.unlink()
                    self.__discord.notify_file_deleted(file)
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
                    self.__discord.notify_file_deleted(file)
        return parents, companion_files

    @staticmethod
    def __get_companion_files(file: Path) -> set[Path]:
        return set(file.parent.glob(f"*.srt")) \
            .union(set(file.parent.glob(f"*.nfo"))) \
            .union(set(file.parent.glob(f"*.metathumb"))) \
            .union(set(file.parent.glob(f"*.png"))) \
            .union(set(file.parent.glob(f"*.jpg"))) \
            .union(set(file.parent.glob(f"*.jpeg"))) \
            .union(set(file.parent.glob(f"@eaDir/*")))
