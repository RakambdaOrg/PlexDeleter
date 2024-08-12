ALTER TABLE `user_group`
    ADD COLUMN `notify_watchlist`                      boolean not null default true,
    ADD COLUMN `notify_requirement_added`              boolean not null default true,
    ADD COLUMN `notify_media_added`                    boolean not null default true,
    ADD COLUMN `notify_media_available`                boolean not null default true,
    ADD COLUMN `notify_media_deleted`                  boolean not null default true,
    ADD COLUMN `notify_requirement_manually_watched`   boolean not null default true,
    ADD COLUMN `notify_requirement_manually_abandoned` boolean not null default true;