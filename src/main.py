import logging
import os
import sys

from api.overseerr_api import Overseerr
from api.tautulli_api import Tautulli
from database import Database
from deleter import Deleter
from notify.discord import Discord
from notify.mailer import Mailer
from notify.notifier import Notifier
from updater import Updater

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s %(levelname)s %(message)s",
    datefmt="%Y-%m-%dT%H:%M:%S%z",
    handlers=[
        logging.StreamHandler(sys.stdout)
    ]
)


def get_env(name: str, required: bool = True, default: str = None) -> str:
    value = os.environ.get(name)
    if value is not None:
        return value
    if required:
        raise RuntimeError(f'Missing env variable {name}')
    return default


if __name__ == '__main__':
    with Database(get_env('DB_HOST'), get_env('DB_USER'), get_env('DB_PASS'), get_env('DB_DB')) as database:
        mailer = Mailer(
            username=get_env('MAIL_USERNAME', required=False),
            password=get_env('MAIL_PASSWORD', required=False),
            server=get_env('MAIL_SERVER'),
            port=int(get_env('MAIL_PORT', required=False, default='0')),
            name_from=get_env('MAIL_FROM', required=False),
            mail_from=get_env('MAIL_MAIL')
        )
        tautulli = Tautulli(get_env('TAUTULLI_URL'), get_env('TAUTULLI_KEY'))
        overseerr = Overseerr(get_env('OVERSEERR_URL'), get_env('OVERSEERR_KEY'))
        discord = Discord(get_env('DISCORD_WEBHOOK', required=False), database)
        updater = Updater(database, tautulli, overseerr, discord)
        notifier = Notifier(database, mailer, get_env('PLEX_SERVER_ID'))
        deleter = Deleter(get_env('REMOTE_PATH'), get_env('LOCAL_PATH'), get_env('DRY_RUN', required=False, default='false').lower() == 'true', database, tautulli, discord)

        updater.update_releasing()
        updater.update_all_groups()

        fully_watched = database.get_fully_watched()
        deleter.delete_all(fully_watched)

        notifier.notify_all_unwatched()
