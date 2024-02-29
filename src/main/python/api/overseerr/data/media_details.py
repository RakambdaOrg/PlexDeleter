from dataclasses import dataclass
from typing import Optional


@dataclass
class MediaDetails:
    rating_key: Optional[int] = None
    overseerr_url: Optional[str] = None
    plex_web_url: Optional[str] = None
    plex_ios_url: Optional[str] = None
    tvdb_id: Optional[int] = None
    tmdb_id: Optional[int] = None
    name: Optional[str] = None
