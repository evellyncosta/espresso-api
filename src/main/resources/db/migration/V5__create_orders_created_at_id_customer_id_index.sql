CREATE INDEX idx_orders_created_at_id_customer_id
    ON orders (created_at, id, customer_id);
