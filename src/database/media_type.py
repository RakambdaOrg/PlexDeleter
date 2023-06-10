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
