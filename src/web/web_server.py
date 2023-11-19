import logging

import flask
from flask import Flask
from waitress import serve

from action.deleter import Deleter
from action.notifier import Notifier
from action.status_updater import StatusUpdater
from action.watch_updater import WatchUpdater
from api.discord.discord_helper import DiscordHelper
from api.overseerr.overseerr_helper import OverseerrHelper
from database.database import Database
from web.homepage import Homepage
from web.web_utils import WebUtils
from web.webhook_overserr import WebhookOverseerr
from web.webhook_radarr import WebhookRadarr
from web.webhook_sonarr import WebhookSonarr
from web.webhook_tautulli import WebhookTautulli


class WebServer:
    def __init__(self, bearer_token: str, basic_token: str, overseerr: OverseerrHelper, database: Database, discord: DiscordHelper, status_updater: StatusUpdater, watch_updater: WatchUpdater, deleter: Deleter, notifier: Notifier):
        self.__logger = logging.getLogger(__name__)
        self.__web_utils = WebUtils([f"Bearer {bearer_token}", f"Basic {basic_token}"], watch_updater, deleter, status_updater, notifier)
        self.__webhook_overseerr = WebhookOverseerr(self.__web_utils, overseerr, database, discord, status_updater, notifier)
        self.__webhook_tautulli = WebhookTautulli(self.__web_utils)
        self.__webhook_sonarr = WebhookSonarr(self.__web_utils, database)
        self.__webhook_radarr = WebhookRadarr(self.__web_utils)
        self.__homepage = Homepage(self.__web_utils, database, overseerr)

        self.__app = Flask("PlexDeleter")
        self.__app.route('/favicon.svg')(self.favicon)

        self.__app.get('/')(self.__homepage.home)
        self.__app.get('/maintenance/full')(self.__web_utils.on_maintenance_full)
        self.__app.get('/maintenance/updates')(self.__web_utils.on_maintenance_updates)
        self.__app.post('/webhook/overseerr')(self.__webhook_overseerr.on_webhook_overseerr)
        self.__app.post('/webhook/tautulli')(self.__webhook_tautulli.on_webhook_tautulli)
        self.__app.post('/webhook/sonarr')(self.__webhook_sonarr.on_webhook_sonarr)
        self.__app.post('/webhook/radarr')(self.__webhook_radarr.on_webhook_radarr)

    def run(self):
        serve(self.__app, host="0.0.0.0", port=8080)

    def favicon(self):
        return flask.send_from_directory('static', 'favicon.svg', mimetype='image/svg+xml')
