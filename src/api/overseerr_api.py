from typing import Optional
import requests


class Overseerr:
    def __init__(self, url, key):
        self.__endpoint = url
        self.__session = requests.Session()
        self.__session.headers.update({'X-Api-Key': key})

    def get_tv_season_episode_count(self, tv_id: int, season_number: int) -> Optional[int]:
        r = self.__session.get(f'{self.__endpoint}/api/v1/tv/{tv_id}/season/{season_number}')
        if r.status_code != 200:
            return None

        data = r.json()
        if 'episodes' not in data:
            return None

        return len(data['episodes'])
