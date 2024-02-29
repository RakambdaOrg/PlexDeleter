import flask
import logging
from database.database import Database
from flask import Response


class Admin:
    def __init__(self, database: Database):
        self.__logger = logging.getLogger(__name__)
        self.__database = database

    def on_form_add_requirement(self) -> str | Response:
        user_persons = self.__database.user_person_get_all()
        return flask.render_template('admin/requirement_add.html', user_persons=user_persons)

    def on_form_abandon_requirement(self) -> str | Response:
        user_groups = self.__database.user_group_get_all()
        return flask.render_template('admin/requirement_abandon.html', user_groups=user_groups)

    def on_form_complete_requirement(self) -> str | Response:
        user_groups = self.__database.user_group_get_all()
        return flask.render_template('admin/requirement_complete.html', user_groups=user_groups)

    def on_form_delete_media(self) -> str | Response:
        medias = self.__database.media_get_all_releasing()
        return flask.render_template('admin/media_delete.html', medias=medias)
