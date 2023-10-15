import logging
import os
import sys

from action.deleter import Deleter
from action.notification.notifier_discord import DiscordNotifier
from action.notification.notifier_discord_thread import DiscordNotifierThread
from action.notification.notifier_mail import MailNotifier
from action.notifier import Notifier
from action.status_updater import StatusUpdater
from action.watch_updater import WatchUpdater
from api.discord.discord_helper import DiscordHelper
from api.mail.mailer import Mailer
from api.overseerr.overseerr_api import OverseerrApi
from api.overseerr.overseerr_helper import OverseerrHelper
from api.radarr.radarr_api import RadarrApi
from api.radarr.radarr_helper import RadarrHelper
from api.sonarr.sonarr_api import SonarrApi
from api.sonarr.sonarr_helper import SonarrHelper
from api.tautulli.tautulli_api import TautulliApi
from api.tautulli.tautulli_helper import TautulliHelper
from database.database import Database
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
    database_host = get_env("DB_HOST")
    database_user = get_env("DB_USER")
    database_password = get_env("DB_PASS")
    database_name = get_env("DB_DB")

    remote_path = get_env("REMOTE_PATH")
    local_path = get_env("LOCAL_PATH")
    discord_webhook = get_env("DISCORD_WEBHOOK", required=False)
    dry_run = get_env("DRY_RUN", required=False, default="false").lower() == "true"
    delete_min_days = int(get_env("DELETE_MIN_DAYS", required=False, default="2"))

    server_auth_bearer = get_env("BEARER_TOKEN")
    server_auth_basic = get_env("BASIC_TOKEN")

    mail_username = get_env("MAIL_USERNAME", required=False)
    mail_password = get_env("MAIL_PASSWORD", required=False)
    mail_host = get_env("MAIL_SERVER")
    mail_port = get_env("MAIL_PORT", required=False, default='0')
    mail_from_name = get_env("MAIL_FROM", required=False)
    mail_from_mail = get_env("MAIL_MAIL")

    tautulli_host = get_env("TAUTULLI_URL")
    tautulli_key = get_env("TAUTULLI_KEY")

    overseerr_host = get_env("OVERSEERR_URL")
    overseerr_key = get_env("OVERSEERR_KEY")

    radarr_host = get_env("RADARR_URL")
    radarr_key = get_env("RADARR_KEY")

    sonarr_host = get_env("SONARR_URL")
    sonarr_key = get_env("SONARR_KEY")

    database = Database(database_host, database_user, database_password, database_name)
    mailer = Mailer(username=mail_username, password=mail_password, server=mail_host, port=int(mail_port), name_from=mail_from_name, mail_from=mail_from_mail)
    discord_helper = DiscordHelper(discord_webhook)
    tautulli_api = TautulliApi(tautulli_host, tautulli_key)
    tautulli_helper = TautulliHelper(tautulli_api)
    overseerr_api = OverseerrApi(overseerr_host, overseerr_key)
    overseerr_helper = OverseerrHelper(overseerr_api)
    radarr_api = RadarrApi(radarr_host, radarr_key)
    radarr_helper = RadarrHelper(radarr_api)
    sonarr_api = SonarrApi(sonarr_host, sonarr_key)
    sonarr_helper = SonarrHelper(sonarr_api)
    discord_notifier = DiscordNotifier(overseerr_helper, discord_helper)
    discord_notifier_thread = DiscordNotifierThread(overseerr_helper, discord_helper)
    mail_notifier = MailNotifier(mailer, overseerr_helper)
    status_updater = StatusUpdater(database, tautulli_helper, overseerr_helper, discord_helper, radarr_helper, sonarr_helper)
    watch_updater = WatchUpdater(database, tautulli_helper, overseerr_helper, discord_helper)
    deleter = Deleter(remote_path, local_path, dry_run, database, tautulli_helper, overseerr_helper, discord_helper, delete_min_days)
    notifier = Notifier(database, mail_notifier, discord_notifier, discord_notifier_thread)
    web_server = WebServer(server_auth_bearer, server_auth_basic, overseerr_helper, database, discord_helper, status_updater, watch_updater, deleter, notifier)
    web_server.run()
