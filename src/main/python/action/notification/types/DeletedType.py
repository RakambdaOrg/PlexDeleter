from action.notification.types.NotifyType import NotifyType


class DeletedType(NotifyType):
    def get_discord_header(self, locale: str) -> str:
        if locale.lower() == "fr":
            return "Média supprimé :"
        return "Media deleted:"

    def get_discord_header_releasing(self, locale: str) -> str:
        return self.get_discord_header(locale)

    def get_subject(self, locale: str) -> str:
        if locale.lower() == "fr":
            return "Plex : média supprimé de la liste de lecture et des téléchargeurs"
        return "Plex: media deleted from watchlist and downloaders"
