DROP TABLE IF EXISTS `character_pvpbook`;
CREATE TABLE `character_pvpbook` (
	`char_id` INT NOT NULL,
	`killed_id` INT NOT NULL,
	`killer_id` INT NOT NULL,
	`death_time` INT NOT NULL,
	`killed_name` varchar(35) NOT NULL,
	`killer_name` varchar(35) NOT NULL,
	`killed_level` TINYINT UNSIGNED NOT NULL,
	`killer_level` TINYINT UNSIGNED NOT NULL,
	`killed_class_id` TINYINT UNSIGNED NOT NULL,
	`killer_class_id` TINYINT UNSIGNED NOT NULL,
	`killed_clan_name` VARCHAR(45) NOT NULL,
	`killer_clan_name` VARCHAR(45) NOT NULL,
	`karma` INT NOT NULL,
	`shared_time` INT NOT NULL,
	PRIMARY KEY  (`char_id`,`killed_id`,`killer_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;