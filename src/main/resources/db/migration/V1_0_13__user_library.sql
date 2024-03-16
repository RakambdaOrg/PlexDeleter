create table user_group_library
(
    id       int auto_increment primary key,
    name     varchar(50) not null,
    group_id int         not null,
    constraint unique (name, group_id),
    constraint FK_UGL_UGid foreign key (group_id) references user_group (id)
        on update cascade
        on delete cascade
);