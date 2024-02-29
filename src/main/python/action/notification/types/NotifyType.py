from abc import ABC, abstractmethod


class NotifyType(ABC):
    @abstractmethod
    def get_discord_header(self, locale: str) -> str:
        pass

    @abstractmethod
    def get_discord_header_releasing(self, locale: str) -> str:
        pass

    @abstractmethod
    def get_subject(self, locale: str) -> str:
        pass
