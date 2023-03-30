import datetime
from dataclasses import dataclass, field


@dataclass(unsafe_hash=True)
class UserGroup:
    id: int = field(hash=True, compare=True)
    name: str = field(hash=False, compare=False)
    mail: str = field(hash=False, compare=False)
    locale: str = field(hash=False, compare=False)
    last_notification: datetime.datetime = field(hash=False, compare=False)

    def __str__(self):
        return f"User group '{self.name}' [id: {self.id}]"
