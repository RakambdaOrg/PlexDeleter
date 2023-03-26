import datetime
from dataclasses import dataclass


@dataclass
class UserGroup:
    id: int
    name: str
    mail: str
    locale: str
    last_notification: datetime.datetime

    def __str__(self):
        return f"User group '{self.name}' [id: {self.id}]"
