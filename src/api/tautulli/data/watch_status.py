from dataclasses import dataclass, field
from typing import Optional, Self


@dataclass
class WatchStatus:
    season: Optional[int]
    episode: int
    watch_percentage: int

    def merge(self, other: Self) -> None:
        self.watch_percentage = max(other.watch_percentage, self.watch_percentage)
