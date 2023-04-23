from abc import ABC

from action.status.user_media_status import UserMediaStatus
from database.media import Media
from database.media_status import MediaStatus
from database.media_type import MediaType


class CommonNotifier(ABC):
    @staticmethod
    def _get_header(locale: str) -> str:
        if locale.lower() == "fr":
            return "Liste des médias en attente de visionnage sur Plex:"
        return "Medias waiting to be watched on Plex:"

    @staticmethod
    def _get_media_body(locale: str, media: Media, user_media_status: UserMediaStatus) -> str:
        if locale.lower() == 'fr':
            media_type = 'Film' if media.type == MediaType.MOVIE else 'Série'
            season = f' - Saison {media.season_number}' if media.season_number else ''
            releasing = ' | En cours de diffusion' if media.status == MediaStatus.RELEASING else ''
            status = f' | Attente EPs {user_media_status.get_all_str()}' if media.type == MediaType.SHOW and user_media_status and not user_media_status.is_all_watched() else ''
        else:
            media_type = 'Movie' if media.type == MediaType.MOVIE else 'Series'
            season = f' - Season {media.season_number}' if media.season_number else ''
            releasing = ' | Releasing' if media.status == MediaStatus.RELEASING else ''
            status = f' | Waiting EPs {user_media_status.get_all_str()}' if media.type == MediaType.SHOW and user_media_status and not user_media_status.is_all_watched() else ''

        return f'{media_type}: {media.name}{season}{releasing}{status}'

    @staticmethod
    def get_subject(locale: str) -> str:
        if locale.lower() == "fr":
            return "Plex: Media en attente"
        return "Plex: Pending media"
