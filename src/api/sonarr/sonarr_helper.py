from typing import Optional

import pydash

from api.sonarr.sonarr_api import SonarrApi


class SonarrHelper:
    def __init__(self, api: SonarrApi):
        self.api = api

    def get_tv_season_episode_percentage(self, tvdb_id: int, season_number: int) -> Optional[(int, int)]:
        data = self.api.get_series(tvdb_id)
        for s in data:
            if "seasons" not in data:
                continue

            season = None
            for sea in data["seasons"]:
                if sea["seasonNumber"] == season_number:
                    season = sea
            if not season:
                continue

            if "percentOfEpisodes" in season:
                return s["percentOfEpisodes"], pydash.get(season, f"totalEpisodeCount", None)

        return None
