from typing import Optional

from api.overseerr.overseerr_api import OverseerrApi
from api.overseerr.plex_urls import PlexUrls
from database.media_type import MediaType


class OverseerrHelper:
    def __init__(self, overseerr: OverseerrApi):
        self.api = overseerr

    def get_tv_season_episode_count(self, tv_id: int, season_number: int) -> Optional[int]:
        data = self.api.get_tv_season_details(tv_id, season_number)
        if "episodes" not in data:
            return None
        return len(data["episodes"])

    def get_plex_rating_key(self, media_id: int, media_type: MediaType) -> Optional[int]:
        rating_key_str = None
        if media_type == MediaType.SHOW:
            rating_key_str = self.api.get_tv_details(media_id)["mediaInfo"]["ratingKey"]
        elif media_type == MediaType.MOVIE:
            rating_key_str = self.api.get_movie_details(media_id)["mediaInfo"]["ratingKey"]

        if not rating_key_str:
            return None
        return int(rating_key_str)

    def get_plex_url(self, media_id: int, media_type: MediaType) -> Optional[PlexUrls]:
        media_info = None
        if media_type == MediaType.SHOW:
            media_info = self.api.get_tv_details(media_id)["mediaInfo"]
        elif media_type == MediaType.MOVIE:
            media_info = self.api.get_movie_details(media_id)["mediaInfo"]

        return PlexUrls(media_info["plexUrl"], media_info["iOSPlexUrl"]) if media_info else None
