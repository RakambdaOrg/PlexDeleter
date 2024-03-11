create table Users
(
    Username varchar(50)  not null primary key,
    Password varchar(500) not null,
    Enabled  boolean      not null
);

create table Authorities
(
    Username  varchar(50) not null,
    Authority varchar(50) not null,
    constraint fk_authorities_users foreign key (Username) references Users (Username)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

create unique index ix_auth_username on Authorities (Username, Authority);