import logging

from flask import Response, request

from database.database import Database


class Api:
    def __init__(self, database: Database):
        self.__logger = logging.getLogger(__name__)
        self.__database = database

    def on_add_requirement(self):
        payload = request.json
        self.__logger.info(f"Received api add requirement call with payload {payload}")

        return Response(status=200)
