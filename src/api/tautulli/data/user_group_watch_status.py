from dataclasses import dataclass, field
from typing import Optional

from api.tautulli.data.user_watch_status import UserWatchStatus
from api.tautulli.data.watch_status import WatchStatus


@dataclass
class UserGroupWatchStatus:
    watch_statuses: dict[int, UserWatchStatus] = field(default_factory=lambda: {})

    def add_watch_status(self, user_id: int, plex_id: int, watch_status: WatchStatus) -> None:
        self.get_watch_status(user_id).add_watch_status(plex_id, watch_status)

    def get_watch_status(self, user_id: int) -> UserWatchStatus:
        if user_id not in self.watch_statuses:
            self.watch_statuses[user_id] = UserWatchStatus()
        return self.watch_statuses[user_id]
