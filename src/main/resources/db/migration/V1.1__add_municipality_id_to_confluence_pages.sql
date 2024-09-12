ALTER TABLE `confluence_pages` ADD COLUMN `municipality_id` VARCHAR(4) NULL;
UPDATE `confluence_pages` SET `municipality_id` = '2281';
ALTER TABLE `confluence_pages` MODIFY `municipality_id` VARCHAR(4) NOT NULL;
ALTER TABLE `confluence_pages` ADD INDEX municipality_id_index(`municipality_id`);