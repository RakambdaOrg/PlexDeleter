from enum import Enum
from typing import Optional, Self


class MediaType(Enum):
    SHOW = 'SHOW'
    MOVIE = 'MOVIE'

    @staticmethod
    def from_overseerr(media_type: str) -> Optional[Self]:
        if media_type == 'tv':
            return MediaType.SHOW
        if media_type == 'movie':
            return MediaType.MOVIE

        return None

    def get_for_display(self, locale: str) -> str:
        if self == MediaType.SHOW:
            if locale == "fr":
                return "Série"
            return "Series"
        if self == MediaType.MOVIE:
            if locale == "fr":
                return "Film"
            return "Movie"

    def get_icon(self) -> Optional[str]:
        if self == MediaType.SHOW:
            return 'tv.svg'
        if self == MediaType.MOVIE:
            return 'movie.svg'

        return None
