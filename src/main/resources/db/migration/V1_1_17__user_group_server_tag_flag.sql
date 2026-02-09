ALTER TABLE `user_group`
    ADD COLUMN IF NOT EXISTS `can_view_server_tags` boolean not null default false after `appear_in_collections`;