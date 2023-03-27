FROM python:3.11-buster

ARG UNAME=pythonuser
ARG UID=1050
ARG GID=100

RUN mkdir -p /usr/src/app
COPY ./docker/entrypoint.sh /entrypoint.sh

RUN chmod +x /entrypoint.sh \
    && /usr/sbin/groupadd -g ${GID} -o ${UNAME} \
    && /usr/sbin/useradd -m -u ${UID} -g ${GID} -o -s /bin/bash ${UNAME} \
    && chown -R ${UNAME}:${UNAME} /usr/src/app

RUN apt-get install gcc wget \
    # https://stackoverflow.com/questions/74429209/mariadb-in-docker-mariadb-connector-python-requires-mariadb-connector-c-3-2
    && wget https://dlm.mariadb.com/2678579/Connectors/c/connector-c-3.3.3/mariadb-connector-c-3.3.3-debian-buster-amd64.tar.gz -O - | tar -zxf - --strip-components=1 -C /usr

ENV LD_PRELOAD=/usr/lib/mariadb/libmariadb.so

USER ${UNAME}
WORKDIR /usr/src/app

COPY requirements.txt /usr/src/app/

RUN pip install --no-cache-dir -r /usr/src/app/requirements.txt

COPY src/ /usr/src/app/

ENTRYPOINT ["/entrypoint.sh"]
