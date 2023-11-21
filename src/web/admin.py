import logging

import flask
from flask import Response

from database.database import Database


class Admin:
    def __init__(self, database: Database):
        self.__logger = logging.getLogger(__name__)
        self.__database = database

    def on_form_add_requirement(self) -> str | Response:
        user_groups = self.__database.user_group_get_all()
        return flask.render_template('admin/requirement.html', user_groups=user_groups)
