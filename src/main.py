import logging
import os
import sys
from api import Api
from database import Database
from deleter import Deleter
from mailer import Mailer
from notifier import Notifier
from updater import Updater

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s %(levelname)s %(message)s",
    datefmt="%Y-%m-%dT%H:%M:%S%z",
    handlers=[
        logging.StreamHandler(sys.stdout)
    ]
)


def get_env(name: str) -> str:
    value = os.environ.get(name)
    if value is None:
        raise RuntimeError(f'Missing env variable {name}')
    return value


if __name__ == '__main__':
    with Database(get_env('DB_HOST'), get_env('DB_USER'), get_env('DB_PASS'), get_env('DB_DB')) as database:
        mailer = Mailer(
            username=get_env('MAIL_USERNAME'),
            password=get_env('MAIL_PASSWORD'),
            server=get_env('MAIL_SERVER'),
            name_from=get_env('MAIL_FROM'),
            mail_from=get_env('MAIL_MAIL')
        )
        tautulli = Api(get_env('TAUTULLI_URL'), get_env('TAUTULLI_KEY'))
        updater = Updater(database, tautulli)
        deleter = Deleter(get_env('REMOTE_PATH'), get_env('LOCAL_PATH'), get_env('DRY_RUN').lower() == 'true', database, tautulli)
        notifier = Notifier(database, mailer, get_env('PLEX_SERVER_ID'))

        updater.update_all_groups()

        fully_watched = database.get_fully_watched()
        deleter.delete_all(fully_watched)

        notifier.notify_all_unwatched()
