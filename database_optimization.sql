USE agricultural_products;

-- Run this script after:
-- 1. database.sql
-- 2. database_extension.sql
-- 3. database_review_extension.sql
--
-- It adds relational constraints and indexes used by the backend business flow.

-- Merge duplicate cart rows before adding the user/product unique key.
CREATE TEMPORARY TABLE tmp_cart_merge AS
SELECT
    MIN(id) AS keep_id,
    user_id,
    product_id,
    SUM(quantity) AS total_quantity,
    MAX(selected) AS selected
FROM cart
GROUP BY user_id, product_id
HAVING COUNT(*) > 1;

UPDATE cart c
JOIN tmp_cart_merge m ON c.id = m.keep_id
SET c.quantity = m.total_quantity,
    c.selected = m.selected;

DELETE c
FROM cart c
JOIN tmp_cart_merge m
  ON c.user_id = m.user_id
 AND c.product_id = m.product_id
 AND c.id <> m.keep_id;

DROP TEMPORARY TABLE tmp_cart_merge;

-- Useful lookup indexes.
ALTER TABLE `category`
    ADD INDEX `idx_category_status_sort` (`status`, `sort`);

ALTER TABLE `product`
    ADD INDEX `idx_product_category_status` (`category_id`, `status`),
    ADD INDEX `idx_product_status_sales` (`status`, `sales`),
    ADD INDEX `idx_product_create_time` (`create_time`);

ALTER TABLE `order`
    ADD INDEX `idx_order_user_time` (`user_id`, `create_time`),
    ADD INDEX `idx_order_status_time` (`status`, `create_time`);

ALTER TABLE `order_item`
    ADD INDEX `idx_order_item_order` (`order_id`),
    ADD INDEX `idx_order_item_product` (`product_id`);

ALTER TABLE `cart`
    ADD UNIQUE KEY `uk_cart_user_product` (`user_id`, `product_id`),
    ADD INDEX `idx_cart_user_selected` (`user_id`, `selected`);

ALTER TABLE `review`
    ADD UNIQUE KEY `uk_review_order_product` (`user_id`, `product_id`, `order_id`),
    ADD INDEX `idx_review_product_status_parent_time` (`product_id`, `status`, `parent_id`, `create_time`);

-- Data integrity constraints.
ALTER TABLE `product`
    ADD CONSTRAINT `fk_product_category`
        FOREIGN KEY (`category_id`) REFERENCES `category` (`id`)
        ON DELETE SET NULL ON UPDATE CASCADE;

ALTER TABLE `order`
    ADD CONSTRAINT `fk_order_user`
        FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
        ON DELETE RESTRICT ON UPDATE CASCADE;

ALTER TABLE `order_item`
    ADD CONSTRAINT `fk_order_item_order`
        FOREIGN KEY (`order_id`) REFERENCES `order` (`id`)
        ON DELETE CASCADE ON UPDATE CASCADE,
    ADD CONSTRAINT `fk_order_item_product`
        FOREIGN KEY (`product_id`) REFERENCES `product` (`id`)
        ON DELETE SET NULL ON UPDATE CASCADE;

ALTER TABLE `cart`
    ADD CONSTRAINT `fk_cart_user`
        FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
        ON DELETE CASCADE ON UPDATE CASCADE,
    ADD CONSTRAINT `fk_cart_product`
        FOREIGN KEY (`product_id`) REFERENCES `product` (`id`)
        ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE `favorite`
    ADD CONSTRAINT `fk_favorite_user`
        FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
        ON DELETE CASCADE ON UPDATE CASCADE,
    ADD CONSTRAINT `fk_favorite_product`
        FOREIGN KEY (`product_id`) REFERENCES `product` (`id`)
        ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE `review`
    ADD CONSTRAINT `fk_review_user`
        FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
        ON DELETE CASCADE ON UPDATE CASCADE,
    ADD CONSTRAINT `fk_review_product`
        FOREIGN KEY (`product_id`) REFERENCES `product` (`id`)
        ON DELETE CASCADE ON UPDATE CASCADE,
    ADD CONSTRAINT `fk_review_order`
        FOREIGN KEY (`order_id`) REFERENCES `order` (`id`)
        ON DELETE SET NULL ON UPDATE CASCADE,
    ADD CONSTRAINT `fk_review_parent`
        FOREIGN KEY (`parent_id`) REFERENCES `review` (`id`)
        ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE `review_like`
    ADD CONSTRAINT `fk_review_like_review`
        FOREIGN KEY (`review_id`) REFERENCES `review` (`id`)
        ON DELETE CASCADE ON UPDATE CASCADE,
    ADD CONSTRAINT `fk_review_like_user`
        FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
        ON DELETE CASCADE ON UPDATE CASCADE;

-- Basic range checks. MySQL 8.0 enforces CHECK constraints.
ALTER TABLE `product`
    ADD CONSTRAINT `ck_product_price_non_negative` CHECK (`price` >= 0),
    ADD CONSTRAINT `ck_product_stock_non_negative` CHECK (`stock` >= 0),
    ADD CONSTRAINT `ck_product_sales_non_negative` CHECK (`sales` >= 0);

ALTER TABLE `cart`
    ADD CONSTRAINT `ck_cart_quantity_positive` CHECK (`quantity` > 0),
    ADD CONSTRAINT `ck_cart_selected` CHECK (`selected` IN (0, 1));

ALTER TABLE `order`
    ADD CONSTRAINT `ck_order_total_non_negative` CHECK (`total_amount` >= 0),
    ADD CONSTRAINT `ck_order_status` CHECK (`status` IN (0, 1, 2, 3, 4));

ALTER TABLE `review`
    ADD CONSTRAINT `ck_review_rating` CHECK (`rating` BETWEEN 0 AND 5),
    ADD CONSTRAINT `ck_review_like_count_non_negative` CHECK (`like_count` >= 0),
    ADD CONSTRAINT `ck_review_status` CHECK (`status` IN (0, 1));
