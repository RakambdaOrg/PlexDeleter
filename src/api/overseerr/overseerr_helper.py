import datetime

import pydash

from api.overseerr.data.media_details import MediaDetails
from api.overseerr.data.request_details import RequestDetails
from api.overseerr.data.season_details import SeasonDetails
from api.overseerr.overseerr_api import OverseerrApi
from database.media_type import MediaType


class OverseerrHelper:
    def __init__(self, overseerr: OverseerrApi):
        self.api = overseerr

    def get_tv_season_details(self, tv_id: int, season_number: int) -> SeasonDetails:
        data = self.api.get_tv_season_details(tv_id, season_number)
        if "episodes" not in data:
            return SeasonDetails()

        episodes = data["episodes"]
        max_air_date = None

        for episode in episodes:
            air_date = pydash.get(episode, f"airDate", None)
            if air_date:
                air_date_date = datetime.datetime.strptime(air_date, "%Y-%m-%d")
                if max_air_date is None or max_air_date < air_date_date:
                    max_air_date = air_date_date

        return SeasonDetails(len(episodes), max_air_date)

    def get_plex_rating_key(self, media_id: int, media_type: MediaType) -> MediaDetails:
        media_details = MediaDetails()

        if media_type == MediaType.SHOW:
            data = self.api.get_tv_details(media_id)
        elif media_type == MediaType.MOVIE:
            data = self.api.get_movie_details(media_id)
        else:
            return media_details

        rating_key = pydash.get(data, f"mediaInfo.ratingKey", None)
        media_info = pydash.get(data, f"mediaInfo", None)
        tvdb_id = pydash.get(data, f"mediaInfo.tvdbId", None)
        tmdb_id = pydash.get(data, f"mediaInfo.tmdbId", None)

        media_details.rating_key = int(rating_key) if rating_key else None
        media_details.overseerr_url = f"{self.api.endpoint}/{media_type.get_overseerr_type()}/{media_id}"
        media_details.plex_web_url = media_info["plexUrl"] if media_info and "plexUrl" in media_info else None
        media_details.plex_ios_url = media_info["iOSPlexUrl"] if media_info and "iOSPlexUrl" in media_info else None
        media_details.tvdb_id = int(tvdb_id) if tvdb_id else None
        media_details.tmdb_id = int(tmdb_id) if tmdb_id else None

        return media_details

    def get_request_details(self, request_id: int) -> RequestDetails:
        data = self.api.get_request(request_id)
        return RequestDetails(
            pydash.get(data, f"requestedBy.plexId", None),
            pydash.get(data, f"tags", None)
        )

    def get_servarr_tags(self, media_type: MediaType):
        if media_type == MediaType.SHOW:
            data = self.api.get_sonarr_service(0)
            return pydash.get(data, f"tags", [])
        elif media_type == MediaType.MOVIE:
            data = self.api.get_radarr_service(0)
            return pydash.get(data, f"tags", [])

        return []
