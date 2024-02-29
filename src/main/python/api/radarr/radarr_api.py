import requests
from array import array


class RadarrApi:
    def __init__(self, url, key):
        self.endpoint = url
        self.__session = requests.Session()
        self.__session.headers.update({'X-Api-Key': key})

    def get_movie(self, tmdb_id: int) -> array:
        r = self.__session.get(f"{self.endpoint}/api/v3/movie?tmdbId={tmdb_id}")
        if r.status_code != 200:
            raise RuntimeError(f"Radarr API returned code {r.status_code}")
        return r.json()
