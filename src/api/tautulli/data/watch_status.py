from dataclasses import dataclass, field
from typing import Optional


@dataclass
class WatchStatus:
    season: Optional[int]
    episode: int
    watch_percentage: int

    def update_progress(self, watch_percentage: int) -> None:
        self.watch_percentage = max(watch_percentage, self.watch_percentage)
