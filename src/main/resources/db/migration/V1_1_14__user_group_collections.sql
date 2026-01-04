ALTER TABLE `user_group`
    ADD COLUMN `appear_in_collections` boolean not null default false after `notify_requirement_manually_abandoned`;
