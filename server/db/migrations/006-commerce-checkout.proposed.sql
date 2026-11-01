-- Deployment-only proposal: product checkout attribution and exact stock reservation state.
-- Stock is decremented in the same transaction as order escrow; no historical backfill or fund writes here.
CREATE TABLE IF NOT EXISTS t_product_order_reservations (
 order_id CHAR(36) NOT NULL PRIMARY KEY COMMENT '关联订单ID，作为预留记录主键',
 product_id CHAR(36) NOT NULL COMMENT '下单商品ID',
 buyer_id CHAR(36) NOT NULL COMMENT '买家用户ID',
 vendor_id CHAR(36) NOT NULL COMMENT '商品卖家及带单人用户ID',
 trip_id CHAR(36) NOT NULL COMMENT '用于履约的行程ID',
 store_id CHAR(36) NULL COMMENT '下单时商品归属店铺ID',
 quantity INT NOT NULL COMMENT '下单商品数量',
 unit_price DECIMAL(12,2) NOT NULL COMMENT '商品成交单价，单位为currency对应币种的主单位',
 goods_amount DECIMAL(12,2) NOT NULL COMMENT '商品货款总额，单位为currency对应币种的主单位',
 service_fee DECIMAL(12,2) NOT NULL COMMENT '订单服务费，单位为currency对应币种的主单位',
 currency CHAR(3) NOT NULL DEFAULT 'CNY' COMMENT '金额币种代码，当前下单写入CNY',
 stock_state VARCHAR(16) NOT NULL DEFAULT 'held' COMMENT '库存占用状态：held已预留、consumed已消耗、released已释放',
 request_hash CHAR(64) NOT NULL COMMENT '下单请求内容哈希，用于核对幂等请求一致性',
 snapshot JSON NOT NULL COMMENT '下单时商品与订单约定JSON快照，用于核对计价和归属',
 created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
 updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '最后更新时间',
 INDEX idx_product_reservation_state(product_id,stock_state),
 INDEX idx_store_order_attribution(store_id,created_at),
 CONSTRAINT chk_product_order_quantity CHECK(quantity BETWEEN 1 AND 100),
 CONSTRAINT chk_product_order_stock_state CHECK(stock_state IN('held','consumed','released')),
 CONSTRAINT fk_product_order_order FOREIGN KEY(order_id) REFERENCES t_orders(id),
 CONSTRAINT fk_product_order_product FOREIGN KEY(product_id) REFERENCES t_products(id),
 CONSTRAINT fk_product_order_store FOREIGN KEY(store_id) REFERENCES t_stores(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品下单时的库存占用、计价与归属快照';
