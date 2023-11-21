import logging
import threading
from threading import Thread
from typing import Optional

from flask import Response

from action.deleter import Deleter
from action.notifier import Notifier
from action.status.user_group_status import UserGroupStatus
from action.status_updater import StatusUpdater
from action.watch_updater import WatchUpdater
from database.user_group import UserGroup


class WebUtils:
    def __init__(self, watch_updater: WatchUpdater, deleter: Deleter, status_updater: StatusUpdater, notifier: Notifier):
        self.__logger = logging.getLogger(__name__)
        self.__watch_updater = watch_updater
        self.__deleter = deleter
        self.__status_updater = status_updater
        self.__notifier = notifier

        self.__lock = threading.RLock()

    def on_maintenance_full(self) -> Response:
        self.__logger.info("Received full maintenance request")
        thread = Thread(target=self.run_maintenance_full)
        thread.start()
        return Response(status=200)

    def on_maintenance_updates(self) -> Response:
        self.__logger.info("Received update maintenance request")
        thread = Thread(target=self.run_maintenance_updates)
        thread.start()
        return Response(status=200)

    def run_maintenance_full(self):
        with self.__lock:
            user_group_statuses = self.run_maintenance_updates()
            self.__deleter.delete()
            self.__notifier.notify_watchlist(user_group_statuses)
            self.__logger.info("Full maintenance done")

    def run_maintenance_updates(self, refresh_status: bool = True, refresh_watch: bool = True, user_id: Optional[int] = None) -> dict[UserGroup, UserGroupStatus]:
        with self.__lock:
            if refresh_status:
                self.__status_updater.update()

            user_group_statuses = {}
            if refresh_watch:
                user_group_statuses = self.__watch_updater.update(user_id)

            self.__logger.info("Updates maintenance done")
            return user_group_statuses
