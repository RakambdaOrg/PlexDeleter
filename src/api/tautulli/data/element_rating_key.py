from dataclasses import dataclass, field
from typing import Optional, Self

from api.tautulli.data.user_watch_status import UserWatchStatus
from api.tautulli.data.watch_status import WatchStatus


@dataclass
class ElementRatingKey:
    rating_key: int
    index: int
    children: dict[int, Self] = field(default_factory=lambda: {})

    def add_child(self, child: Self) -> None:
        self.children[child.index] = child

    def get_child(self, child_index: int) -> Optional[Self]:
        return self.children[child_index] if child_index in self.children else None
