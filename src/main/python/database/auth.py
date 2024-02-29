from dataclasses import dataclass, field


@dataclass(unsafe_hash=True)
class Auth:
    type: str = field(hash=True, compare=True)
    username: str = field(hash=True, compare=True)

    def __str__(self):
        return f"{self.type} '{self.username}'"
