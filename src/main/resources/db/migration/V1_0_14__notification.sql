ALTER TABLE `user_group`
    DROP COLUMN `display`;

CREATE TABLE notification
(
    id    int auto_increment primary key          not null,
    type  enum ('MAIL', 'DISCORD_THREAD', 'NONE') not null,
    value varchar(256)                            not null,
    constraint unique (type, value)
);

INSERT INTO notification(`type`, `value`)
SELECT `notification_type`, `notification_value`
FROM `user_group`;

ALTER TABLE `user_group`
    ADD COLUMN `notification_id` int AFTER `notification_value`;
ALTER TABLE `user_group`
    ADD CONSTRAINT FK_UG_NotificationId foreign key (`notification_id`) references notification (`Id`)
        on update cascade
        on delete set null;

ALTER TABLE `user_group`
    ADD COLUMN `notification_media_added_id` int AFTER `notification_id`;
ALTER TABLE `user_group`
    ADD CONSTRAINT FK_UG_NotificationMediaAddedId foreign key (`notification_media_added_id`) references notification (`Id`)
        on update cascade
        on delete set null;

UPDATE `user_group` UG
SET `notification_id`             = (SELECT N.id FROM `notification` N WHERE N.type = UG.notification_type AND N.value = UG.notification_value),
    `notification_media_added_id` = (SELECT N.id FROM `notification` N WHERE N.type = UG.notification_type AND N.value = UG.notification_value);

ALTER TABLE `user_group`
    DROP COLUMN `notification_type`;
ALTER TABLE `user_group`
    DROP COLUMN `notification_value`;