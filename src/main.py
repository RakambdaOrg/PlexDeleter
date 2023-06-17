import logging
import os
import sys

from action.notification.notifier_discord import DiscordNotifier
from action.notification.notifier_discord_thread import DiscordNotifierThread
from action.notification.notifier_mail import MailNotifier
from action.status_updater import StatusUpdater
from api.overseerr.overseerr_api import OverseerrApi
from api.overseerr.overseerr_helper import OverseerrHelper
from action.deleter import Deleter
from api.discord.discord_helper import DiscordHelper
from api.radarr.radarr_api import RadarrApi
from api.radarr.radarr_helper import RadarrHelper
from api.sonarr.sonarr_api import SonarrApi
from api.sonarr.sonarr_helper import SonarrHelper
from api.tautulli.tautulli_api import TautulliApi
from api.tautulli.tautulli_helper import TautulliHelper
from database.database import Database
from api.mail.mailer import Mailer
from action.notifier import Notifier
from action.watch_updater import WatchUpdater
from web.web_server import WebServer


def get_env(name: str, required: bool = True, default: str = None) -> str:
    value = os.environ.get(name)
    if value is not None:
        return value
    if required:
        raise RuntimeError(f"Missing env variable {name}")
    return default


logging.basicConfig(
    level=logging.DEBUG if get_env("LOG_LEVEL_DEBUG", False, None) else logging.INFO,
    format="%(asctime)s %(levelname)s %(message)s",
    datefmt="%Y-%m-%dT%H:%M:%S%z",
    handlers=[
        logging.StreamHandler(sys.stdout)
    ]
)

if __name__ == "__main__":
    database = Database(get_env("DB_HOST"), get_env("DB_USER"), get_env("DB_PASS"), get_env("DB_DB"))
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

    radarr_api = RadarrApi(get_env("RADARR_URL"), get_env("RADARR_KEY"))
    radarr_helper = RadarrHelper(radarr_api)

    sonarr_api = SonarrApi(get_env("SONARR_URL"), get_env("SONARR_KEY"))
    sonarr_helper = SonarrHelper(sonarr_api)

    discord_notifier = DiscordNotifier(overseerr_helper, discord_helper)
    discord_notifier_thread = DiscordNotifierThread(overseerr_helper, discord_helper)
    mail_notifier = MailNotifier(mailer, overseerr_helper)

    status_updater = StatusUpdater(database, tautulli_helper, overseerr_helper, discord_helper, radarr_helper, sonarr_helper)
    watch_updater = WatchUpdater(database, tautulli_helper, overseerr_helper, discord_helper)
    deleter = Deleter(get_env("REMOTE_PATH"), get_env("LOCAL_PATH"), get_env("DRY_RUN", required=False, default="false").lower() == "true", database, tautulli_helper, overseerr_helper, discord_helper, int(get_env("DELETE_MIN_DAYS", required=False, default="2")))
    notifier = Notifier(database, mail_notifier, discord_notifier, discord_notifier_thread)

    web_server = WebServer(get_env("BEARER_TOKEN"), overseerr_helper, database, discord_helper, status_updater, watch_updater, deleter, notifier)
    web_server.run()
