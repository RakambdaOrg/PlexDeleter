import requests
from array import array


class SonarrApi:
    def __init__(self, url, key):
        self.endpoint = url
        self.__session = requests.Session()
        self.__session.headers.update({'X-Api-Key': key})

    def get_series(self, tvdb_id: int) -> array:
        r = self.__session.get(f"{self.endpoint}/api/v3/series?tvdbId={tvdb_id}&includeSeasonImages=false")
        if r.status_code != 200:
            raise RuntimeError(f"Sonarr API returned code {r.status_code}")
        return r.json()
