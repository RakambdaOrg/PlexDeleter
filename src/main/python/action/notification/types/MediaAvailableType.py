from action.notification.types.NotifyType import NotifyType


class MediaAvailableType(NotifyType):
    def get_discord_header(self, locale: str) -> str:
        if locale.lower() == "fr":
            return "Média de votre liste de lecture disponible :"
        return "Media from your watchlist available:"

    def get_discord_header_releasing(self, locale: str) -> str:
        return self.get_discord_header(locale)

    def get_subject(self, locale: str) -> str:
        if locale.lower() == "fr":
            return "Plex : Disponible"
        return "Plex: Available"
