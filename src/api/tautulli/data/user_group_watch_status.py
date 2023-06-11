from dataclasses import dataclass, field
from typing import Optional, Self

from api.tautulli.data.user_watch_status import UserWatchStatus
from api.tautulli.data.watch_status import WatchStatus


@dataclass
class UserGroupWatchStatus:
    user_watch_statuses: dict[int, UserWatchStatus] = field(default_factory=lambda: {})
    searched_rating_keys: set[int] = field(default_factory=lambda: set())

    def add_watch_status(self, user_id: int, plex_id: int, watch_status: WatchStatus) -> None:
        self.get_user_watch_status(user_id).add_watch_status(plex_id, watch_status)

    def get_user_watch_status(self, user_id: int) -> UserWatchStatus:
        if user_id not in self.user_watch_statuses:
            self.user_watch_statuses[user_id] = UserWatchStatus()
        return self.user_watch_statuses[user_id]

    def rating_key_searched(self, rating_key: int) -> bool:
        return rating_key in self.searched_rating_keys

    def merge(self, other: Self) -> None:
        self.searched_rating_keys.update(other.searched_rating_keys)
        for user_id, status in other.user_watch_statuses.items():
            if user_id in self.user_watch_statuses:
                self.user_watch_statuses[user_id].merge(status)
            else:
                self.user_watch_statuses[user_id] = status
