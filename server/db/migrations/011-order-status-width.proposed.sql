-- Deployment-only widening for existing status cancelled_no_fault (18 characters).
-- Preserve the existing public state protocol. Do not shrink an already wider column.
-- Requires the intended 82-table business database to be selected before execution.
SET @kuaixie_order_status_ddl = IF(
 EXISTS(SELECT 1 FROM information_schema.columns
        WHERE table_schema=DATABASE() AND table_name='t_orders' AND column_name='status'
          AND data_type='varchar' AND character_maximum_length<32),
 'ALTER TABLE t_orders MODIFY COLUMN status VARCHAR(32) NOT NULL DEFAULT ''requesting'' COMMENT ''订单状态机''',
 'SELECT ''Order status widening skipped; verify existing definition in deployment preflight'' AS migration_status'
);
PREPARE kuaixie_order_status_stmt FROM @kuaixie_order_status_ddl;
EXECUTE kuaixie_order_status_stmt;
DEALLOCATE PREPARE kuaixie_order_status_stmt;
SET @kuaixie_order_status_ddl = NULL;
