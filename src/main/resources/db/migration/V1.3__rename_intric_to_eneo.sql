-- Rename intric_group_id column to eneo_group_id
ALTER TABLE `confluence_pages` CHANGE COLUMN `intric_group_id` `eneo_group_id` VARCHAR (36) NOT NULL;
