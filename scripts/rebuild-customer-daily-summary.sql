-- Reconstrói o resumo diário usado pelo relatório de maiores consumidores.
--
-- Diferentemente da migration V6, este script pode ser executado novamente
-- depois que a migration já estiver registrada pelo Flyway.

BEGIN;

CREATE TABLE IF NOT EXISTS customer_daily_summary (
    customer_id BIGINT NOT NULL,
    summary_date DATE NOT NULL,
    total_orders BIGINT NOT NULL,
    total_items BIGINT NOT NULL,
    total_spent NUMERIC(14, 2) NOT NULL,
    CONSTRAINT pk_customer_daily_summary
        PRIMARY KEY (customer_id, summary_date),
    CONSTRAINT fk_customer_daily_summary_customer
        FOREIGN KEY (customer_id)
        REFERENCES customer (id)
);

TRUNCATE TABLE customer_daily_summary;

INSERT INTO customer_daily_summary (
    customer_id,
    summary_date,
    total_orders,
    total_items,
    total_spent
)
SELECT
    o.customer_id,
    o.created_at::DATE AS summary_date,
    COUNT(DISTINCT o.id) AS total_orders,
    SUM(oi.quantity) AS total_items,
    SUM(oi.quantity * oi.unit_price) AS total_spent
FROM orders o
JOIN order_item oi
    ON oi.order_id = o.id
GROUP BY o.customer_id, o.created_at::DATE;

CREATE INDEX IF NOT EXISTS idx_customer_daily_summary_date_customer_id
    ON customer_daily_summary (summary_date, customer_id);

ANALYZE customer_daily_summary;

COMMIT;
