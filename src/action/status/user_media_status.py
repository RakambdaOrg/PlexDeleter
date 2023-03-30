from dataclasses import dataclass, field


@dataclass
class UserMediaStatus:
    missing_episode_indexes: list[int] = field(default_factory=lambda: [])

    def is_all_watched(self) -> bool:
        return len(self.missing_episode_indexes) <= 0

    def add_index(self, name: int) -> None:
        self.missing_episode_indexes.append(name)

    def add_unknown_index(self) -> None:
        self.add_index(-1)

    def get_all_str(self) -> str:
        return ', '.join(map(lambda x: str(x), sorted(self.missing_episode_indexes)))
