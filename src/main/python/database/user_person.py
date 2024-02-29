from dataclasses import dataclass, field


@dataclass(unsafe_hash=True)
class UserPerson:
    id: int = field(hash=True, compare=True)
    name: str = field(hash=False, compare=False)
    plex_id: int = field(hash=False, compare=False)

    def __str__(self):
        return f"User person '{self.name}' [id: {self.id}]"
