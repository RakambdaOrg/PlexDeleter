import logging
import smtplib
from email.mime.multipart import MIMEMultipart
from email.mime.text import MIMEText
from email.utils import formatdate, formataddr


class Mailer:
    def __init__(self, username: str, password: str, server: str, port: int, name_from: str, mail_from: str, use_tls: bool = False):
        self.__logger = logging.getLogger(__name__)
        self.__smtp = None
        self.__password = password
        self.__username = username
        self.__server = server
        self.__port = port
        self.__tls = use_tls
        self.__from_name = name_from if name_from else mail_from
        self.__from_mail = mail_from

    def send(self, mail_to: list[str], subject: str, plain_body: str, html_body: str = None) -> dict[str, tuple[int, bytes]]:
        try:
            message = self.__create_mail(mail_to, subject, plain_body, html_body)
            self.__logger.info(f"Sending mail to {mail_to}: {message.as_string()}")
            return self.__get_or_create_smtp().sendmail(self.__from_mail, mail_to, message.as_string())
        except Exception as error:
            self.__logger.error("Failed to send message", exc_info=error)
            return {}

    def __is_smtp_connected(self) -> bool:
        if self.__smtp is None:
            return False
        try:
            status = self.__smtp.noop()[0]
        except Exception:
            status = -1
        return True if status == 250 else False

    def __get_or_create_smtp(self) -> smtplib.SMTP:
        if not self.__is_smtp_connected():
            self.__smtp = smtplib.SMTP_SSL(self.__server, self.__port)
            if self.__tls:
                self.__smtp.starttls()
            if self.__username and self.__password:
                self.__smtp.login(self.__username, self.__password)
        return self.__smtp

    def __create_mail(self, mail_to: list[str], subject: str, plain_body: str, html_body: str = None) -> MIMEMultipart:
        message = MIMEMultipart("alternative")
        message["From"] = formataddr((self.__from_name, self.__from_mail))
        message["To"] = ', '.join(mail_to)
        message["Date"] = formatdate(localtime=True)
        message["Subject"] = subject

        message.attach(MIMEText(plain_body, "plain", _charset="UTF-8"))
        if html_body:
            message.attach(MIMEText(html_body, "html", _charset="UTF-8"))
        return message
