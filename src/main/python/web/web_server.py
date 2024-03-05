import logging
from action.deleter import Deleter
from action.notifier import Notifier
from action.status_updater import StatusUpdater
from action.watch_updater import WatchUpdater
from api.discord.discord_helper import DiscordHelper
from api.overseerr.overseerr_helper import OverseerrHelper
from database.database import Database
from flask import Flask, request
from flask_httpauth import HTTPBasicAuth, HTTPTokenAuth
from typing import Optional
from waitress import serve
from web.admin import Admin
from web.api import Api
from web.homepage import Homepage
from web.web_utils import WebUtils
from web.webhook.webhook_ombi import WebhookOmbi
from web.webhook.webhook_overserr import WebhookOverseerr
from web.webhook.webhook_radarr import WebhookRadarr
from web.webhook.webhook_sonarr import WebhookSonarr
from web.webhook.webhook_tautulli import WebhookTautulli


class WebServer:
    def __init__(self, overseerr: OverseerrHelper, database: Database, discord: DiscordHelper,
                 status_updater: StatusUpdater, watch_updater: WatchUpdater, deleter: Deleter, notifier: Notifier):
        self.__logger = logging.getLogger(__name__)
        web_utils = WebUtils(database, watch_updater, deleter, status_updater, notifier, discord)
        admin = Admin(database)
        api = Api(database, web_utils, overseerr, notifier)
        homepage = Homepage(database, overseerr)
        webhook_ombi = WebhookOmbi(web_utils, overseerr)
        webhook_overseerr = WebhookOverseerr(web_utils, overseerr)
        webhook_radarr = WebhookRadarr()
        webhook_sonarr = WebhookSonarr(database)
        webhook_tautulli = WebhookTautulli(web_utils)

        self.__app = Flask("PlexDeleter")

        basic_auth = HTTPBasicAuth()
        bearer_auth = HTTPTokenAuth()
        access_token_auth = HTTPTokenAuth(header="Access-Token")

        @self.__app.route('/')
        def on_home():
            return homepage.on_call(include_hidden=False)

        @self.__app.route('/admin')
        @basic_auth.login_required
        def on_admin_home():
            return homepage.on_call(include_hidden=True)

        @self.__app.route('/admin/media/delete')
        @basic_auth.login_required
        def on_admin_media_delete():
            return admin.on_form_delete_media()

        @self.__app.route('/admin/requirement/add')
        @basic_auth.login_required
        def on_admin_requirement_add():
            return admin.on_form_add_requirement()

        @self.__app.route('/admin/requirement/abandon')
        @basic_auth.login_required
        def on_admin_requirement_abandon():
            return admin.on_form_abandon_requirement()

        @self.__app.route('/admin/requirement/complete')
        @basic_auth.login_required
        def on_admin_requirement_complete():
            return admin.on_form_complete_requirement()

        @self.__app.route('/api/media/delete', methods=['POST'])
        @basic_auth.login_required
        def on_api_media_delete():
            return api.on_delete_media(request.form)

        @self.__app.route('/api/requirement/add', methods=['POST'])
        @basic_auth.login_required
        def on_api_requirement_add():
            return api.on_add_requirement(request.form)

        @self.__app.route('/api/requirement/abandon', methods=['POST'])
        @basic_auth.login_required
        def on_api_requirement_abandon():
            return api.on_abandon_requirement(request.form)

        @self.__app.route('/api/requirement/complete', methods=['POST'])
        @basic_auth.login_required
        def on_api_requirement_complete():
            return api.on_complete_requirement(request.form)

        @basic_auth.verify_password
        def verify_password(username, password) -> Optional[str]:
            user = database.get_auth("BASIC", username, password)
            if user:
                self.__app.logger.info('Authorized %s with BASIC', user.username)
                return user.username
            self.__app.logger.warning('Authorization failed for BASIC %s %s', username, password)
            return None

        @bearer_auth.verify_token
        def verify_password(token) -> Optional[str]:
            user = database.get_auth("BEARER", None, token)
            if user:
                self.__app.logger.info('Authorized %s with BEARER', user.username)
                return user.username
            self.__app.logger.warning('Authorization failed for BEARER %s', token)
            return None

    def run(self):
        serve(self.__app, host="0.0.0.0", port=8080)
