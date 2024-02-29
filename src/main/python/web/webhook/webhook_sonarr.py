import logging
from database.database import Database
from flask import Response


class WebhookSonarr:
    def __init__(self, database: Database):
        self.__logger = logging.getLogger(__name__)
        self.__database = database

    def on_call(self, payload: dict) -> Response:
        payload_type = payload["eventType"]
        if payload_type != 'Grab':
            return Response(status=204, response="Event type isn't grab")

        if 'series' not in payload:
            return Response(status=204, response="Series payload not found")
        series = payload["series"]

        if 'episodes' in series:
            return Response(status=204, response="Episodes payload not found")
        episodes = series["episodes"]
        title = series["title"]
        tvdb_id = series["tvdbId"]

        for episode in episodes:
            season_number = episode["seasonNumber"]
            episode_number = episode["episodeNumber"]
            if tvdb_id and season_number and episode_number:
                self.__logger.info(f"Setting episode count to {episode_number} for show {title} (Season {season_number}) of tvdb {tvdb_id}")
                self.__database.media_tvdb_id_set_episode(tvdb_id, season_number, episode_number)

        return Response(status=200)
