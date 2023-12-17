import logging
from threading import Thread
from typing import Optional

import flask
from flask import Flask, request, Response
from flask_httpauth import HTTPBasicAuth, HTTPTokenAuth
from waitress import serve

from action.deleter import Deleter
from action.notifier import Notifier
from action.status_updater import StatusUpdater
from action.watch_updater import WatchUpdater
from api.discord.discord_helper import DiscordHelper
from api.overseerr.overseerr_helper import OverseerrHelper
from database.database import Database
from web.admin import Admin
from web.api import Api
from web.homepage import Homepage
from web.web_utils import WebUtils
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
        webhook_overseerr = WebhookOverseerr(web_utils, overseerr)
        webhook_radarr = WebhookRadarr()
        webhook_sonarr = WebhookSonarr(database)
        webhook_tautulli = WebhookTautulli(web_utils)

        self.__app = Flask("PlexDeleter")

        basic_auth = HTTPBasicAuth()
        bearer_auth = HTTPTokenAuth()

        @self.__app.before_request
        def log_request_info():
            if not request.path or not request.path.startswith("/static/"):
                return
            self.__app.logger.info('Received %s request on %s', request.method, request.path)
            self.__app.logger.debug('Headers: %s', request.headers)

        @self.__app.after_request
        def log_request_info(response: Response) -> Response:
            if (not request.path or not request.path.startswith("/static/")) and response.status_code == 200:
                return response

            response_data = '<<Passthrough>>' if response.direct_passthrough else response.get_data(True)[:50].replace("\n", " § ")
            self.__app.logger.info(f'Done handling {request.method} request on {request.path} : {response.status_code} ({response_data})')

            return response

        @self.__app.route('/favicon.svg')
        def on_favicon():
            return flask.send_from_directory('static', 'favicon.svg', mimetype='image/svg+xml')

        @self.__app.route('/')
        def on_home():
            return homepage.on_call(include_hidden=False)

        @self.__app.route('/admin')
        @basic_auth.login_required
        def on_admin_home():
            return homepage.on_call(include_hidden=True)

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

        @self.__app.route('/maintenance/full')
        @bearer_auth.login_required
        def on_maintenance_full():
            thread = Thread(target=web_utils.run_maintenance_full)
            thread.start()
            return Response(status=200)

        @self.__app.route('/maintenance/updates')
        @bearer_auth.login_required
        def on_maintenance_updates():
            thread = Thread(target=web_utils.run_maintenance_updates)
            thread.start()
            return Response(status=200)

        @self.__app.route('/webhook/overseerr', methods=["POST"])
        @bearer_auth.login_required
        def on_webhook_overseerr():
            return webhook_overseerr.on_call(request.json)

        @self.__app.route('/webhook/radarr', methods=["POST"])
        @basic_auth.login_required
        def on_webhook_radarr():
            return webhook_radarr.on_call()

        @self.__app.route('/webhook/sonarr', methods=["POST"])
        @basic_auth.login_required
        def on_webhook_sonarr():
            return webhook_sonarr.on_call(request.json)

        @self.__app.route('/webhook/tautulli', methods=["POST"])
        @bearer_auth.login_required
        def on_webhook_tautulli():
            return webhook_tautulli.on_call(request.json)

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
