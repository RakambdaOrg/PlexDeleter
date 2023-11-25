import logging

from flask import Response

from database.database import Database


class Api:
    def __init__(self, database: Database):
        self.__logger = logging.getLogger(__name__)
        self.__database = database

    def on_add_requirement(self):
        return Response(status=200)
