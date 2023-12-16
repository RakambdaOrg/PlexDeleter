from typing import Optional

import pydash

from api.sonarr.sonarr_api import SonarrApi


class SonarrHelper:
    def __init__(self, api: SonarrApi):
        self.api = api
        self.__logger = logging.getLogger(__name__)

    def get_tv_season_episode(self, tvdb_id: int, season_number: int) -> Optional[tuple[int, int]]:
        try:
            data = self.api.get_series(tvdb_id)
            for match in data:
                if "seasons" not in match:
                    continue

                matched_season = None
                for season in match["seasons"]:
                    if season["seasonNumber"] == season_number:
                        matched_season = season
                if not matched_season:
                    continue
                if "statistics" not in matched_season:
                    continue

                statistics = matched_season["statistics"]
                if "episodeCount" in statistics:
                    return statistics["episodeFileCount"], pydash.get(statistics, f"totalEpisodeCount", None)
        except RuntimeError as error:
            self.__logger.error(f"Failed to get sonarr media details for tvdb_id {tvdb_id} and season {season_number}", exc_info=error)

        return None
