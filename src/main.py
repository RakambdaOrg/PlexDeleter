import logging
import os
import sys

from action.status_updater import StatusUpdater
from api.overseerr.overseerr_api import OverseerrApi
from api.overseerr.overseerr_helper import OverseerrHelper
from action.deleter import Deleter
from api.discord.discord_helper import DiscordHelper
from api.tautulli.tautulli_api import TautulliApi
from api.tautulli.tautulli_helper import TautulliHelper
from database.database import Database
from api.mail.mailer import Mailer
from action.notifier import Notifier
from action.watch_updater import WatchUpdater

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
        raise RuntimeError(f"Missing env variable {name}")
    return default


if __name__ == "__main__":
    with Database(get_env("DB_HOST"), get_env("DB_USER"), get_env("DB_PASS"), get_env("DB_DB")) as database:
        mailer = Mailer(
            username=get_env("MAIL_USERNAME", required=False),
            password=get_env("MAIL_PASSWORD", required=False),
            server=get_env("MAIL_SERVER"),
            port=int(get_env("MAIL_PORT", required=False, default='0')),
            name_from=get_env("MAIL_FROM", required=False),
            mail_from=get_env("MAIL_MAIL")
        )
        discord_helper = DiscordHelper(get_env("DISCORD_WEBHOOK", required=False))

        tautulli_api = TautulliApi(get_env("TAUTULLI_URL"), get_env("TAUTULLI_KEY"))
        tautulli_helper = TautulliHelper(tautulli_api)

        overseerr_api = OverseerrApi(get_env("OVERSEERR_URL"), get_env("OVERSEERR_KEY"))
        overseerr_helper = OverseerrHelper(overseerr_api)

        status_updater = StatusUpdater(database, tautulli_helper, overseerr_helper, discord_helper)
        watch_updater = WatchUpdater(database, tautulli_helper, overseerr_helper, discord_helper)
        deleter = Deleter(get_env("REMOTE_PATH"), get_env("LOCAL_PATH"), get_env("DRY_RUN", required=False, default="false").lower() == "true", database, tautulli_helper, overseerr_helper, discord_helper)
        notifier = Notifier(database, mailer, get_env("PLEX_SERVER_ID"))

        status_updater.update()
        watch_updater.update()
        deleter.delete()
        notifier.notify()
