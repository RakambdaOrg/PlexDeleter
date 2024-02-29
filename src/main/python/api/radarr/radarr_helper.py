from api.radarr.radarr_api import RadarrApi
from typing import Optional


class RadarrHelper:
    def __init__(self, api: RadarrApi):
        self.api = api

    def has_file(self, tmdb_id: int) -> Optional[bool]:
        data = self.api.get_movie(tmdb_id)

        for m in data:
            if "hasFile" in m:
                return m["hasFile"]

        return None
