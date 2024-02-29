from action.notification.types.NotifyType import NotifyType


class CompletedType(NotifyType):
    def get_discord_header(self, locale: str) -> str:
        if locale.lower() == "fr":
            return "Nouveau média complété manuellement :"
        return "New media manually completed:"

    def get_discord_header_releasing(self, locale: str) -> str:
        return self.get_discord_header(locale)

    def get_subject(self, locale: str) -> str:
        if locale.lower() == "fr":
            return "Plex : Media complété manuellement"
        return "Plex: Media manually completed"
