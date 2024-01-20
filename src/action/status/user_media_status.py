import datetime
from dataclasses import dataclass, field


@dataclass
class UserMediaStatus:
    missing_episode_indexes: list[int] = field(default_factory=lambda: [])
    size: int = field(default=0)
    available_since: datetime.datetime = field(default_factory=lambda: datetime.datetime.fromtimestamp(0))

    def is_all_watched(self) -> bool:
        return len(self.missing_episode_indexes) <= 0

    def add_index(self, name: int) -> None:
        self.missing_episode_indexes.append(name)

    def add_unknown_index(self) -> None:
        self.add_index(-1)

    def set_size(self, size: int) -> None:
        self.size = size

    def set_available_since(self, available_since: datetime.datetime) -> None:
        self.available_since = available_since

    def get_all_str(self) -> str:
        return ', '.join(map(lambda x: str(x), sorted(self.missing_episode_indexes)))

    def get_size(self) -> int:
        return self.size

    def get_available_since(self) -> datetime.datetime:
        return self.available_since
