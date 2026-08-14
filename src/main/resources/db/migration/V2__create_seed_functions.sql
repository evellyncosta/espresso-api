CREATE OR REPLACE FUNCTION seed_customers(p_quantity BIGINT)
RETURNS BIGINT
LANGUAGE plpgsql
AS $$
DECLARE
    v_inserted BIGINT;
BEGIN
    IF p_quantity IS NULL OR p_quantity <= 0 THEN
        RAISE EXCEPTION 'Seed quantity must be greater than zero';
    END IF;

    INSERT INTO customer (
        name,
        email,
        created_at,
        status
    )
    SELECT
        format('Customer %s', series.customer_number),
        format('customer%s@example.com', series.customer_number),
        customer_dates.created_at,
        CASE
            WHEN random() < 0.90 THEN 'ACTIVE'
            ELSE 'INACTIVE'
        END
    FROM generate_series(1, p_quantity) AS series(customer_number)
    CROSS JOIN LATERAL (
        SELECT (
            CURRENT_TIMESTAMP::TIMESTAMP
            - random() * INTERVAL '3 years'
        ) AS created_at
        WHERE series.customer_number IS NOT NULL
    ) AS customer_dates;

    GET DIAGNOSTICS v_inserted = ROW_COUNT;

    RETURN v_inserted;
END;
$$;


CREATE OR REPLACE FUNCTION seed_products(p_quantity BIGINT)
RETURNS BIGINT
LANGUAGE plpgsql
AS $$
DECLARE
    v_inserted BIGINT;
BEGIN
    IF p_quantity IS NULL OR p_quantity <= 0 THEN
        RAISE EXCEPTION 'Seed quantity must be greater than zero';
    END IF;

    INSERT INTO product (
        name,
        price,
        status,
        created_at
    )
    SELECT
        format('Product %s', series.product_number),
        round((10 + random() * 990)::NUMERIC, 2),
        CASE
            WHEN random() < 0.90 THEN 'ACTIVE'
            ELSE 'INACTIVE'
        END,
        product_dates.created_at
    FROM generate_series(1, p_quantity) AS series(product_number)
    CROSS JOIN LATERAL (
        SELECT (
            CURRENT_TIMESTAMP::TIMESTAMP
            - random() * INTERVAL '3 years'
        ) AS created_at
        WHERE series.product_number IS NOT NULL
    ) AS product_dates;

    GET DIAGNOSTICS v_inserted = ROW_COUNT;

    RETURN v_inserted;
END;
$$;


CREATE OR REPLACE FUNCTION seed_orders(p_quantity BIGINT)
RETURNS BIGINT
LANGUAGE plpgsql
AS $$
DECLARE
    v_customer_ids BIGINT[];
    v_inserted BIGINT;
BEGIN
    IF p_quantity IS NULL OR p_quantity <= 0 THEN
        RAISE EXCEPTION 'Seed quantity must be greater than zero';
    END IF;

    SELECT array_agg(id ORDER BY id)
    INTO v_customer_ids
    FROM customer;

    IF v_customer_ids IS NULL OR cardinality(v_customer_ids) = 0 THEN
        RAISE EXCEPTION 'Cannot seed orders: no customers found';
    END IF;

    INSERT INTO orders (
        customer_id,
        status,
        total_amount,
        created_at,
        updated_at
    )
    SELECT
        v_customer_ids[
            1 + floor(random() * cardinality(v_customer_ids))::INTEGER
        ],
        CASE
            WHEN status_values.status_roll < 0.30 THEN 'DELIVERED'
            WHEN status_values.status_roll < 0.50 THEN 'SHIPPED'
            WHEN status_values.status_roll < 0.67 THEN 'PROCESSING'
            WHEN status_values.status_roll < 0.80 THEN 'PAID'
            WHEN status_values.status_roll < 0.88 THEN 'CREATED'
            WHEN status_values.status_roll < 0.94 THEN 'PAYMENT_PENDING'
            WHEN status_values.status_roll < 0.97 THEN 'CANCELED'
            WHEN status_values.status_roll < 0.99 THEN 'PAYMENT_FAILED'
            ELSE 'REFUNDED'
        END,
        round((20 + random() * 1980)::NUMERIC, 2),
        order_dates.created_at,
        order_dates.created_at
            + random() * (CURRENT_TIMESTAMP::TIMESTAMP - order_dates.created_at)
    FROM generate_series(1, p_quantity) AS series(order_number)
    CROSS JOIN LATERAL (
        SELECT random() AS status_roll
        WHERE series.order_number IS NOT NULL
    ) AS status_values
    CROSS JOIN LATERAL (
        SELECT (
            CURRENT_TIMESTAMP::TIMESTAMP
            - random() * INTERVAL '3 years'
        ) AS created_at
        WHERE series.order_number IS NOT NULL
    ) AS order_dates;

    GET DIAGNOSTICS v_inserted = ROW_COUNT;

    RETURN v_inserted;
END;
$$;


CREATE OR REPLACE FUNCTION seed_order_items(p_quantity BIGINT)
RETURNS BIGINT
LANGUAGE plpgsql
AS $$
DECLARE
    v_order_ids BIGINT[];
    v_product_ids BIGINT[];
    v_inserted BIGINT;
BEGIN
    IF p_quantity IS NULL OR p_quantity <= 0 THEN
        RAISE EXCEPTION 'Seed quantity must be greater than zero';
    END IF;

    SELECT array_agg(id ORDER BY id)
    INTO v_order_ids
    FROM orders;

    IF v_order_ids IS NULL OR cardinality(v_order_ids) = 0 THEN
        RAISE EXCEPTION 'Cannot seed order items: no orders found';
    END IF;

    SELECT array_agg(id ORDER BY id)
    INTO v_product_ids
    FROM product;

    IF v_product_ids IS NULL OR cardinality(v_product_ids) = 0 THEN
        RAISE EXCEPTION 'Cannot seed order items: no products found';
    END IF;

    INSERT INTO order_item (
        order_id,
        product_id,
        quantity,
        unit_price
    )
    SELECT
        v_order_ids[
            1 + floor(random() * cardinality(v_order_ids))::INTEGER
        ],
        v_product_ids[
            1 + floor(random() * cardinality(v_product_ids))::INTEGER
        ],
        floor(random() * 5)::INTEGER + 1,
        round((10 + random() * 990)::NUMERIC, 2)
    FROM generate_series(1, p_quantity) AS series(item_number);

    GET DIAGNOSTICS v_inserted = ROW_COUNT;

    RETURN v_inserted;
END;
$$;
