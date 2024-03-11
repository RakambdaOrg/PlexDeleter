ALTER TABLE `UserGroup`
    MODIFY COLUMN `NotificationType` enum ('MAIL', 'DISCORD_THREAD', 'NONE') not null;