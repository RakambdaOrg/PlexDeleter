CREATE TABLE user_credentials
(
    credential_id                varchar(1000) not null,
    user_entity_user_id          varchar(1000) not null,
    public_key                   blob          not null,
    signature_count              bigint,
    uv_initialized               boolean,
    backup_eligible              boolean       not null,
    authenticator_transports     varchar(1000),
    public_key_credential_type   varchar(100),
    backup_state                 boolean       not null,
    attestation_object           blob,
    attestation_client_data_json blob,
    created                      timestamp,
    last_used                    timestamp,
    label                        varchar(1000) not null,
    PRIMARY KEY (credential_id)
) charset = utf8;

create table user_entities
(
    id           varchar(1000) not null,
    name         varchar(100)  not null,
    display_name varchar(200),
    primary key (id)
) charset = utf8;