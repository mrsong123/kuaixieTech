-- Deployment-only proposal: product checkout attribution and exact stock reservation state.
-- Stock is decremented in the same transaction as order escrow; no historical backfill or fund writes here.
CREATE TABLE IF NOT EXISTS t_product_order_reservations (
 order_id CHAR(36) NOT NULL PRIMARY KEY,
 product_id CHAR(36) NOT NULL,
 buyer_id CHAR(36) NOT NULL,
 vendor_id CHAR(36) NOT NULL,
 trip_id CHAR(36) NOT NULL,
 store_id CHAR(36) NULL,
 quantity INT NOT NULL,
 unit_price DECIMAL(12,2) NOT NULL,
 goods_amount DECIMAL(12,2) NOT NULL,
 service_fee DECIMAL(12,2) NOT NULL,
 currency CHAR(3) NOT NULL DEFAULT 'CNY',
 stock_state VARCHAR(16) NOT NULL DEFAULT 'held',
 request_hash CHAR(64) NOT NULL,
 snapshot JSON NOT NULL,
 created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
 INDEX idx_product_reservation_state(product_id,stock_state),
 INDEX idx_store_order_attribution(store_id,created_at),
 CONSTRAINT chk_product_order_quantity CHECK(quantity BETWEEN 1 AND 100),
 CONSTRAINT chk_product_order_stock_state CHECK(stock_state IN('held','consumed','released')),
 CONSTRAINT fk_product_order_order FOREIGN KEY(order_id) REFERENCES t_orders(id),
 CONSTRAINT fk_product_order_product FOREIGN KEY(product_id) REFERENCES t_products(id),
 CONSTRAINT fk_product_order_store FOREIGN KEY(store_id) REFERENCES t_stores(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
