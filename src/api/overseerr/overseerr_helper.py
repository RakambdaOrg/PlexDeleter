from typing import Optional

import pydash

from src.api.overseerr.overseerr_api import OverseerrApi
from src.api.overseerr.plex_urls import PlexUrls
from src.database.media_type import MediaType


class OverseerrHelper:
    def __init__(self, overseerr: OverseerrApi):
        self.api = overseerr

    def get_tv_season_episode_count(self, tv_id: int, season_number: int) -> Optional[int]:
        data = self.api.get_tv_season_details(tv_id, season_number)
        if "episodes" not in data:
            return None
        return max(map(lambda x: x["episodeNumber"], data["episodes"]))

    def get_plex_rating_key(self, media_id: int, media_type: MediaType) -> Optional[int]:
        rating_key = None
        if media_type == MediaType.SHOW:
            data = self.api.get_tv_details(media_id)
            rating_key = pydash.get(data, f"mediaInfo.ratingKey", None)
        elif media_type == MediaType.MOVIE:
            data = self.api.get_movie_details(media_id)
            rating_key = pydash.get(data, f"mediaInfo.ratingKey", None)

        return int(rating_key) if rating_key else None

    def get_plex_url(self, media_id: int, media_type: MediaType) -> Optional[PlexUrls]:
        media_info = None
        if media_type == MediaType.SHOW:
            data = self.api.get_tv_details(media_id)
            media_info = pydash.get(data, f"mediaInfo", None)
        elif media_type == MediaType.MOVIE:
            data = self.api.get_movie_details(media_id)
            media_info = pydash.get(data, f"mediaInfo", None)

        return PlexUrls(media_info["plexUrl"], media_info["iOSPlexUrl"]) if media_info else None
