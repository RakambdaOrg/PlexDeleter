ALTER TABLE `notification`
    MODIFY COLUMN type enum ('MAIL', 'DISCORD', 'DISCORD_THREAD', 'NONE') not null;