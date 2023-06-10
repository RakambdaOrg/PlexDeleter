from typing import Optional

import pydash

from api.overseerr.media_urls import MediaUrls
from api.overseerr.overseerr_api import OverseerrApi
from database.media_type import MediaType


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

    def get_plex_url(self, media_id: int, media_type: MediaType) -> MediaUrls:
        overseerr_url = None
        media_info = None
        if media_type == MediaType.SHOW:
            overseerr_url = f"{self.api.endpoint}/tv/{media_id}"
            data = self.api.get_tv_details(media_id)
            media_info = pydash.get(data, f"mediaInfo", None)
        elif media_type == MediaType.MOVIE:
            overseerr_url = f"{self.api.endpoint}/movie/{media_id}"
            data = self.api.get_movie_details(media_id)
            media_info = pydash.get(data, f"mediaInfo", None)

        return MediaUrls(overseerr_url,
                         media_info["plexUrl"] if media_info and "plexUrl" in media_info else None,
                         media_info["iOSPlexUrl"] if media_info and "iOSPlexUrl" in media_info else None)

    def get_tvdb_id(self, media_id: int, media_type: MediaType) -> Optional[int]:
        tvdb_id = None
        if media_type == MediaType.SHOW:
            data = self.api.get_tv_details(media_id)
            tvdb_id = pydash.get(data, f"mediaInfo.tvdbId", None)
        elif media_type == MediaType.MOVIE:
            data = self.api.get_movie_details(media_id)
            tvdb_id = pydash.get(data, f"mediaInfo.tvdbId", None)

        return int(tvdb_id) if tvdb_id else None

    def get_tmdb_id(self, media_id: int, media_type: MediaType) -> Optional[int]:
        tmdb_id = None
        if media_type == MediaType.SHOW:
            data = self.api.get_tv_details(media_id)
            tmdb_id = pydash.get(data, f"mediaInfo.tmdbId", None)
        elif media_type == MediaType.MOVIE:
            data = self.api.get_movie_details(media_id)
            tmdb_id = pydash.get(data, f"mediaInfo.tmdbId", None)

        return int(tmdb_id) if tmdb_id else None

    def get_requester_plex_id(self, request_id: int) -> Optional[int]:
        data = self.api.get_request(request_id)
        return pydash.get(data, f"requestedBy.plexId", None)
