import logging
import smtplib
from email.mime.multipart import MIMEMultipart
from email.mime.text import MIMEText
from email.utils import formatdate, formataddr


class Mailer:
    def __init__(self, username: str, password: str, server: str, name_from: str, mail_from: str, smtp_port: int = 587, use_tls: bool = True):
        self.__logger = logging.getLogger(__name__)
        self.__smtp = None
        self.__imap = None
        self.__password = password
        self.__username = username
        self.__server = server
        self.__smtp_port = smtp_port
        self.__tls = use_tls
        self.__from_name = name_from
        self.__from_mail = mail_from

    def __is_smtp_connected(self) -> bool:
        if self.__smtp is None:
            return False
        try:
            status = self.__smtp.noop()[0]
        except Exception:
            status = -1
        return True if status == 250 else False

    def __get_or_create_smtp(self):
        if not self.__is_smtp_connected():
            self.__smtp = smtplib.SMTP(self.__server, self.__smtp_port)
            if self.__tls:
                self.__smtp.starttls()
            self.__smtp.login(self.__username, self.__password)
        return self.__smtp

    def __create_mail(self, subject: str, body: str, mail_to: list[str]) -> MIMEMultipart:
        message = MIMEMultipart()
        message['From'] = formataddr((self.__from_name, self.__from_mail))
        message['To'] = ', '.join(mail_to)
        message['Date'] = formatdate(localtime=True)
        message['Subject'] = subject

        message.attach(MIMEText(body))
        return message

    def send_mail(self, subject: str, body: str, mail_to: list[str]) -> dict[str, tuple[int, bytes]]:
        try:
            message = self.__create_mail(subject, body, mail_to)
            return self.__get_or_create_smtp().sendmail(self.__from_mail, mail_to, message.as_string())
        except Exception as e:
            self.__logger.error('Failed to send message', exc_info=e)
            return {}
