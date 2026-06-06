alter table products
    add column stock_on_hand integer not null default 0;

alter table products
    add column reserved_stock integer not null default 0;

alter table products
    add constraint ck_products_stock
        check (
            stock_on_hand >= 0
                and reserved_stock >= 0
                and reserved_stock <= stock_on_hand
            );

create index idx_products_stock
    on products(stock_on_hand, reserved_stock);

create table cart_idempotency_keys (
                                       id uuid primary key,
                                       customer_id uuid not null,
                                       endpoint varchar(128) not null,
                                       idempotency_key varchar(128) not null,
                                       request_hash varchar(128) not null,
                                       cart_id uuid,
                                       status varchar(32) not null,
                                       created_at timestamp with time zone not null default now(),
                                       updated_at timestamp with time zone,
                                       version bigint not null default 0,

                                       constraint uk_cart_idempotency_customer_endpoint_key
                                           unique (customer_id, endpoint, idempotency_key),
                                       constraint fk_cart_idempotency_customer
                                           foreign key (customer_id)
                                               references customers(id),
                                       constraint fk_cart_idempotency_cart
                                           foreign key (cart_id)
                                               references carts(id)
);

create index idx_cart_idempotency_customer
    on cart_idempotency_keys(customer_id);

create index idx_cart_idempotency_key
    on cart_idempotency_keys(idempotency_key);

create index idx_cart_idempotency_cart
    on cart_idempotency_keys(cart_id);

create table stock_reservations (
                                    id uuid primary key,
                                    cart_id uuid not null,
                                    product_id uuid not null,
                                    product_code integer not null,
                                    quantity integer not null,
                                    status varchar(32) not null,
                                    created_at timestamp with time zone not null default now(),
                                    updated_at timestamp with time zone,
                                    version bigint not null default 0,

                                    constraint fk_stock_reservations_cart
                                        foreign key (cart_id)
                                            references carts(id)
                                            on delete cascade,
                                    constraint fk_stock_reservations_product
                                        foreign key (product_id)
                                            references products(id),
                                    constraint ck_stock_reservations_quantity
                                        check (quantity > 0)
);

create index idx_stock_reservations_cart
    on stock_reservations(cart_id);

create index idx_stock_reservations_product
    on stock_reservations(product_id);

create index idx_stock_reservations_status
    on stock_reservations(status);