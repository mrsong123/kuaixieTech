-- Deployment proposal only: explicit ownership-checked storefront associations.
-- No changes to historical orders, prices, commission rates, insurance or account balances.
CREATE TABLE IF NOT EXISTS t_store_products (
 store_id CHAR(36) NOT NULL COMMENT '店铺ID',
 product_id CHAR(36) NOT NULL COMMENT '商品ID',
 created_by CHAR(36) NOT NULL COMMENT '建立店铺商品关联的用户ID',
 created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '关联创建时间',
 PRIMARY KEY(store_id,product_id),
 UNIQUE KEY uq_store_product(product_id),
 CONSTRAINT fk_store_product_store FOREIGN KEY(store_id) REFERENCES t_stores(id),
 CONSTRAINT fk_store_product_product FOREIGN KEY(product_id) REFERENCES t_products(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='店铺与商品的归属关联';
