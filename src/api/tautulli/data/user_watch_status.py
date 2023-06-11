from dataclasses import dataclass, field
from typing import Optional

from api.tautulli.data.watch_status import WatchStatus


@dataclass
class UserWatchStatus:
    watch_statuses: dict[int, WatchStatus] = field(default_factory=lambda: {})

    def add_watch_status(self, plex_id: int, watch_status: WatchStatus) -> None:
        if plex_id in self.watch_statuses:
            self.watch_statuses[plex_id].update_progress(watch_status.watch_percentage)
        else:
            self.watch_statuses[plex_id] = watch_status

    def get_watch_percentage(self, rating_key: int) -> int:
        if rating_key not in self.watch_statuses:
            return 0
        return self.watch_statuses[rating_key].watch_percentage
