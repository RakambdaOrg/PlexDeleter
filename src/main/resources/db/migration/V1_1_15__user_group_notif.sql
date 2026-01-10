ALTER TABLE `user_group`
    ADD COLUMN `notify_media_watched` boolean not null default true after 'notify_media_deleted';