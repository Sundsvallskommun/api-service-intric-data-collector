CREATE TABLE IF NOT EXISTS `confluence_pages` (
    `page_id` VARCHAR(16) NOT NULL,
    `intric_group_id` VARCHAR(36) NOT NULL,
    `blob_id` VARCHAR(36) NOT NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL,
    PRIMARY KEY (`page_id`)
);
