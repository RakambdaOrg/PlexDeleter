from typing import Optional
import array
import requests


class OverseerrApi:
    def __init__(self, url, key):
        self.endpoint = url
        self.__session = requests.Session()
        self.__session.headers.update({'X-Api-Key': key})

    def get_movie_details(self, tv_id: int) -> array:
        r = self.__session.get(f"{self.endpoint}/api/v1/movie/{tv_id}")
        if r.status_code != 200:
            raise RuntimeError(f"Overseerr API returned code {r.status_code}")
        return r.json()

    def get_tv_details(self, tv_id: int) -> Optional[array]:
        r = self.__session.get(f"{self.endpoint}/api/v1/tv/{tv_id}")
        if r.status_code != 200:
            return None
        return r.json()

    def get_tv_season_details(self, tv_id: int, season_number: int) -> array:
        r = self.__session.get(f"{self.endpoint}/api/v1/tv/{tv_id}/season/{season_number}")
        if r.status_code != 200:
            raise RuntimeError(f"Overseerr API returned code {r.status_code}")
        return r.json()
