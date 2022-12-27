import json
import logging
import os

from api import Api
from deleter import Deleter
from media import Media
from mediatype import MediaType
from requireduser import RequiredUser
from scanner import Scanner

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(message)s",
    handlers=[
        logging.StreamHandler()
    ]
)


def get_env(name: str) -> str:
    value = os.environ.get(name)
    if value is None:
        raise RuntimeError(f'Missing env variable {name}')
    return value


if __name__ == '__main__':
    medias = []
    with open(get_env('CONFIG_LOCATION')) as file:
        read_json = json.load(file)
        for element in read_json['media']:
            medias.append(Media(MediaType[element['type'].upper()],
                                element['ratingKey'],
                                element['name'],
                                [RequiredUser[x.upper()] for x in element['users']]))

    tautulli = Api(get_env('TAUTULLI_URL'), get_env('TAUTULLI_KEY'))
    scanner = Scanner(tautulli)
    deleter = Deleter(get_env('REMOTE_PATH'), get_env('LOCAL_PATH'))

    to_delete = []
    for media in medias:
        to_delete += scanner.process_media(media) or []

    deleter.delete_all(to_delete)
