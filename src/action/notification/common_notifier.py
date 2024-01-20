import datetime
from abc import ABC, abstractmethod
from typing import Optional

import humanize

from action.notification.types.NotifyType import NotifyType
from action.status.user_group_status import UserGroupStatus
from action.status.user_media_status import UserMediaStatus
from database.media import Media
from database.media_status import MediaStatus
from database.media_type import MediaType
from database.user_group import UserGroup


class CommonNotifier(ABC):
    @abstractmethod
    def notify(self, user_group: UserGroup, medias: list[Media], user_group_status: Optional[UserGroupStatus], notify_type: NotifyType) -> None:
        pass

    @staticmethod
    def _get_header_watchlist(locale: str) -> str:
        if locale.lower() == "fr":
            return "Liste des médias en attente de visionnage sur Plex :"
        return "Medias waiting to be watched on Plex:"

    @staticmethod
    def _get_header_releasing_watchlist(locale: str) -> str:
        if locale.lower() == "fr":
            return "Medias en cours de téléchargement :"
        return "Medias waiting to be downloaded:"

    @staticmethod
    def _get_header_requirement_added(locale: str) -> str:
        if locale.lower() == "fr":
            return "Nouveau média à regarder ajouté car vous l'avez demandé ou avez regardé la saison précédente :"
        return "New media to watch added because you requested it or watched previous season:"

    @staticmethod
    def _get_header_media_available(locale: str) -> str:
        if locale.lower() == "fr":
            return "Média de votre liste de lecture disponible :"
        return "Media from your watchlist available:"

    def _get_media_body(self, locale: str, media: Media, user_media_status: Optional[UserMediaStatus]) -> str:
        if locale.lower() == 'fr':
            media_type = 'Film' if media.type == MediaType.MOVIE else 'Série'
            season = f' - Saison {media.season_number}' if media.season_number else ''
            releasing = ' | En cours de diffusion' if media.status == MediaStatus.RELEASING else ''
            status = f' | Attente EPs {user_media_status.get_all_str()}' if user_media_status and media.type == MediaType.SHOW and not user_media_status.is_all_watched() else ''
            size = f' | Taille {humanize.naturalsize(user_media_status.get_size())}' if user_media_status and user_media_status.get_size() else ''
            available_for = f' | Disponible depuis {self.__days_diff(datetime.datetime.now(), user_media_status.get_available_since())} jour(s)' if user_media_status and user_media_status.get_available_since() else ''
        else:
            media_type = 'Movie' if media.type == MediaType.MOVIE else 'Series'
            season = f' - Season {media.season_number}' if media.season_number else ''
            releasing = ' | Releasing' if media.status == MediaStatus.RELEASING else ''
            status = f' | Waiting EPs {user_media_status.get_all_str()}' if user_media_status and media.type == MediaType.SHOW and user_media_status and not user_media_status.is_all_watched() else ''
            size = f' | Size {humanize.naturalsize(user_media_status.get_size())}' if user_media_status and user_media_status.get_size() else ''
            available_for = f' | Available for {self.__days_diff(datetime.datetime.now(), user_media_status.get_available_since())} day(s)' if user_media_status and user_media_status.get_available_since() else ''

        return f'{media_type}: {media.name}{season}{releasing}{status}{size}{available_for}'

    @staticmethod
    def __days_diff(start: datetime.datetime, end: datetime.datetime):
        return (start - end).days

    @staticmethod
    def _get_subject_watchlist(locale: str) -> str:
        if locale.lower() == "fr":
            return "Plex : Media en attente"
        return "Plex: Pending media"

    @staticmethod
    def _get_subject_requirement_added(locale: str) -> str:
        if locale.lower() == "fr":
            return "Plex : Media ajouté a votre liste de lecture"
        return "Plex: Media added to your watch list"

    @staticmethod
    def _get_subject_media_available(locale: str) -> str:
        if locale.lower() == "fr":
            return "Plex : Disponible"
        return "Plex: Available"
