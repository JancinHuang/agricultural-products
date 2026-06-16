USE `agricultural_products`;

DROP PROCEDURE IF EXISTS add_banner_style_column;

DELIMITER //
CREATE PROCEDURE add_banner_style_column(
  IN p_column_name VARCHAR(64),
  IN column_definition TEXT
)
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'banner'
      AND COLUMN_NAME = p_column_name
  ) THEN
    SET @ddl = CONCAT('ALTER TABLE `banner` ADD COLUMN `', p_column_name, '` ', column_definition);
    PREPARE stmt FROM @ddl;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;
END//
DELIMITER ;

CALL add_banner_style_column('show_title', 'tinyint DEFAULT ''1'' COMMENT ''是否显示标题：0隐藏，1显示'' AFTER `image`');
CALL add_banner_style_column('title_color', 'varchar(32) DEFAULT ''#ffffff'' COMMENT ''标题颜色'' AFTER `show_title`');
CALL add_banner_style_column('title_font_size', 'int DEFAULT ''42'' COMMENT ''标题字号(px)'' AFTER `title_color`');
CALL add_banner_style_column('title_font_weight', 'int DEFAULT ''700'' COMMENT ''标题字重'' AFTER `title_font_size`');
CALL add_banner_style_column('subtitle_color', 'varchar(32) DEFAULT ''rgba(255,255,255,0.9)'' COMMENT ''副标题颜色'' AFTER `title_font_weight`');
CALL add_banner_style_column('subtitle_font_size', 'int DEFAULT ''18'' COMMENT ''副标题字号(px)'' AFTER `subtitle_color`');
CALL add_banner_style_column('show_button', 'tinyint DEFAULT ''1'' COMMENT ''是否显示按钮：0隐藏，1显示'' AFTER `subtitle_font_size`');
CALL add_banner_style_column('button_color', 'varchar(32) DEFAULT ''#2e7d32'' COMMENT ''按钮颜色'' AFTER `show_button`');

DROP PROCEDURE IF EXISTS add_banner_style_column;

UPDATE `banner`
SET
  `show_title` = COALESCE(`show_title`, 1),
  `title_color` = COALESCE(NULLIF(`title_color`, ''), '#ffffff'),
  `title_font_size` = COALESCE(`title_font_size`, 42),
  `title_font_weight` = COALESCE(`title_font_weight`, 700),
  `subtitle_color` = COALESCE(NULLIF(`subtitle_color`, ''), 'rgba(255,255,255,0.9)'),
  `subtitle_font_size` = COALESCE(`subtitle_font_size`, 18),
  `show_button` = COALESCE(`show_button`, 1),
  `button_color` = COALESCE(NULLIF(`button_color`, ''), '#2e7d32');
