create table if not exists Auth
(
    Type     enum ('BEARER', 'BASIC', 'NOTIFICATION') not null,
    Username varchar(64)                              not null,
    Password varchar(64)                              not null,
    primary key (Type, Username)
) engine = InnoDB;

create table if not exists Media
(
    Id           int auto_increment primary key,
    OverseerrId  int                                                          not null,
    TvdbId       int                                                          null,
    Name         varchar(128)                                                 not null,
    Season       int                                                          null,
    ElementCount int                                                          null,
    Type         enum ('SHOW', 'MOVIE')                                       not null,
    Status       enum ('FINISHED', 'RELEASING', 'MANUAL') default 'RELEASING' not null,
    ActionStatus enum ('TO_DELETE', 'DELETED', 'KEEP')    default 'TO_DELETE' not null,
    constraint IDX_UNIQUE_Show_id_season unique (OverseerrId, Type, Season)
) engine = InnoDB;

create table if not exists MediaRequirement
(
    MediaId int                                                        not null,
    GroupId int                                                        not null,
    Status  enum ('WAITING', 'WATCHED', 'ABANDONED') default 'WAITING' not null,
    primary key (MediaId, GroupId),
    constraint FK_MR_Mid foreign key (MediaId) references Media (Id)
        on update cascade
        on delete cascade,
    constraint FK_MR_UGid foreign key (GroupId) references UserGroup (Id)
        on update cascade
        on delete cascade
) engine = InnoDB;

create table if not exists UserGroup
(
    Id                int auto_increment primary key,
    Name              varchar(16)                                        not null,
    NotificationType  enum ('MAIL', 'DISCORD', 'DISCORD_THREAD', 'NONE') not null,
    NotificationValue varchar(256)                                       not null,
    Locale            varchar(2)                                         not null,
    LastNotification  timestamp  default current_timestamp()             not null on update current_timestamp(),
    Display           tinyint(1) default 1                               not null,
    ServarrTag        varchar(32)                                        null,
    constraint Name unique (Name)
) engine = InnoDB;

create table if not exists UserMapping
(
    GroupId  int not null,
    PersonId int not null,
    primary key (GroupId, PersonId),
    constraint FK_UA_PUid foreign key (PersonId) references UserPerson (Id),
    constraint FK_UA_Uid foreign key (GroupId) references UserGroup (Id)
) engine = InnoDB;

create table if not exists UserPerson
(
    Id     int auto_increment primary key,
    Name   varchar(32) not null,
    PlexId int(9)      not null,
    constraint PlexId unique (PlexId)
) engine = InnoDB;