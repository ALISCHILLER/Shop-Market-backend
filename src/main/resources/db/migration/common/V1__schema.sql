create sequence if not exists cart_code_seq start with 100001 increment by 1;

create table customers (
                           id uuid primary key,
                           customer_code varchar(64) not null,
                           customer_name varchar(255) not null,
                           mobile varchar(32),
                           phone varchar(32),
                           center varchar(255),
                           national_code varchar(32),
                           password_hash varchar(255) not null,
                           salt varchar(255),
                           password_change_required boolean not null default false,
                           role varchar(32) not null default 'CUSTOMER',
                           enabled boolean not null default true,
                           created_at timestamp with time zone not null default now(),
                           updated_at timestamp with time zone,
                           version bigint not null default 0,

                           constraint uk_customers_customer_code unique (customer_code),
                           constraint ck_customers_role check (role in ('ADMIN', 'CUSTOMER'))
);

create table product_categories (
                                    product_category_code integer primary key,
                                    product_category_name varchar(255),
                                    product_category_image text,
                                    product_category_image_unselect text,

                                    constraint ck_product_categories_code_positive
                                        check (product_category_code > 0)
);

create table products (
                          id uuid primary key,
                          product_name varchar(255),
                          product_code integer not null,
                          full_name_kala1 varchar(255),
                          unit1 varchar(64),
                          unitid1 varchar(64),
                          convert_factor1 integer not null default 1,
                          full_name_kala2 varchar(255),
                          unit2 varchar(64),
                          convert_factor2 integer not null default 1,
                          unitid2 varchar(64),
                          product_group_code integer not null,
                          price bigint not null default 0,
                          is_discounts boolean not null default false,
                          is_tax boolean not null default true,
                          product_image text,
                          created_at timestamp with time zone not null default now(),
                          updated_at timestamp with time zone,
                          version bigint not null default 0,

                          constraint uk_products_product_code unique (product_code),
                          constraint fk_products_product_category
                              foreign key (product_group_code)
                                  references product_categories(product_category_code),
                          constraint ck_products_positive_codes_and_factors
                              check (
                                  product_code > 0
                                      and product_group_code > 0
                                      and convert_factor1 > 0
                                      and convert_factor2 > 0
                                      and price >= 0
                                  )
);

create table discounts (
                           id uuid primary key,
                           product_id uuid not null,
                           discount_percent integer not null,
                           from_number integer not null,
                           end_number integer not null,
                           created_at timestamp with time zone not null default now(),
                           updated_at timestamp with time zone,
                           version bigint not null default 0,

                           constraint fk_discounts_product
                               foreign key (product_id)
                                   references products(id)
                                   on delete cascade,
                           constraint ck_discounts_valid_values
                               check (
                                   discount_percent between 0 and 100
                                       and from_number >= 1
                                       and end_number >= from_number
                                   )
);

create table banners (
                         id uuid primary key,
                         banner_image text not null,
                         banner_name varchar(255) not null,
                         created_at timestamp with time zone not null default now(),
                         updated_at timestamp with time zone,
                         version bigint not null default 0
);

create table customer_addresses (
                                    id uuid primary key,
                                    customer_id uuid not null,
                                    center_name varchar(255) not null,
                                    customer_address text not null,
                                    customer_mobile varchar(32) not null,
                                    customer_phone varchar(32) not null,
                                    latitude double precision,
                                    longitude double precision,
                                    is_default boolean not null default false,
                                    created_at timestamp with time zone not null default now(),
                                    updated_at timestamp with time zone,
                                    version bigint not null default 0,

                                    constraint fk_customer_addresses_customer
                                        foreign key (customer_id)
                                            references customers(id)
                                            on delete cascade,
                                    constraint ck_customer_addresses_geo_pair
                                        check (
                                            (latitude is null and longitude is null)
                                                or
                                            (
                                                latitude between -90 and 90
                                                    and longitude between -180 and 180
                                                )
                                            )
);

create table payment_terms (
                               id uuid primary key,
                               name varchar(255) not null,
                               dead_line integer not null default 0,
                               payment_kind varchar(32) not null,
                               immediate_discount_percent integer not null default 0,
                               receipt_discount_percent integer not null default 0,
                               cheque_discount_percent integer not null default 0,
                               active boolean not null default true,
                               created_at timestamp with time zone not null default now(),
                               updated_at timestamp with time zone,
                               version bigint not null default 0,

                               constraint ck_payment_terms_valid_values
                                   check (
                                       dead_line >= 0
                                           and payment_kind in ('IMMEDIATE', 'RECEIPT', 'CHEQUE')
                                           and immediate_discount_percent between 0 and 100
                                           and receipt_discount_percent between 0 and 100
                                           and cheque_discount_percent between 0 and 100
                                       )
);

create table carts (
                       id uuid primary key,
                       cart_code integer not null,
                       customer_id uuid not null,
                       customer_address_id uuid not null,
                       payment_term_id uuid not null,
                       customer_name_snapshot varchar(255) not null,
                       customer_address_snapshot text not null,
                       status_code varchar(32) not null default 'REGISTERED',
                       status_name varchar(128) not null,
                       status_color varchar(32) not null,
                       sales_date date not null,
                       subtotal bigint not null default 0,
                       discount_total bigint not null default 0,
                       tax_total bigint not null default 0,
                       total bigint not null default 0,
                       created_at timestamp with time zone not null default now(),
                       updated_at timestamp with time zone,
                       version bigint not null default 0,

                       constraint uk_carts_cart_code unique (cart_code),
                       constraint fk_carts_customer
                           foreign key (customer_id)
                               references customers(id),
                       constraint fk_carts_customer_address
                           foreign key (customer_address_id)
                               references customer_addresses(id),
                       constraint fk_carts_payment_term
                           foreign key (payment_term_id)
                               references payment_terms(id),
                       constraint ck_carts_money_non_negative
                           check (
                               subtotal >= 0
                                   and discount_total >= 0
                                   and tax_total >= 0
                                   and total >= 0
                               )
);

create table cart_items (
                            id uuid primary key,
                            cart_id uuid not null,
                            product_id uuid not null,
                            product_code integer not null,
                            product_name varchar(255) not null,
                            product_image_url text,
                            quantity integer not null,
                            price bigint not null default 0,
                            discount bigint not null default 0,
                            tax bigint not null default 0,
                            total bigint not null default 0,
                            created_at timestamp with time zone not null default now(),
                            updated_at timestamp with time zone,
                            version bigint not null default 0,

                            constraint fk_cart_items_cart
                                foreign key (cart_id)
                                    references carts(id)
                                    on delete cascade,
                            constraint fk_cart_items_product
                                foreign key (product_id)
                                    references products(id),
                            constraint ck_cart_items_valid_values
                                check (
                                    quantity > 0
                                        and quantity <= 1000
                                        and price >= 0
                                        and discount >= 0
                                        and tax >= 0
                                        and total >= 0
                                    )
);

create table refresh_tokens (
                                id uuid primary key,
                                customer_id uuid not null,
                                token_hash varchar(255) not null,
                                expires_at timestamp with time zone not null,
                                revoked_at timestamp with time zone,
                                ip_address varchar(64),
                                user_agent text,
                                created_at timestamp with time zone not null default now(),
                                updated_at timestamp with time zone,
                                version bigint not null default 0,

                                constraint uk_refresh_tokens_token_hash unique (token_hash),
                                constraint fk_refresh_tokens_customer
                                    foreign key (customer_id)
                                        references customers(id)
);

create table audit_logs (
                            id uuid primary key,
                            actor_customer_id uuid,
                            actor_customer_code varchar(64),
                            actor_customer_name varchar(255),
                            action varchar(128) not null,
                            entity_type varchar(128) not null,
                            entity_id varchar(128),
                            old_value jsonb,
                            new_value jsonb,
                            ip_address varchar(64),
                            user_agent text,
                            description text,
                            created_at timestamp with time zone not null default now()
);

create unique index ux_customer_addresses_one_default
    on customer_addresses(customer_id)
    where is_default = true;

create index idx_customers_role
    on customers(role);

create index idx_customers_enabled
    on customers(enabled);

create index idx_customers_created_at
    on customers(created_at);

create index idx_product_categories_name
    on product_categories(product_category_name);

create index idx_products_group_code
    on products(product_group_code);

create index idx_products_product_name
    on products(product_name);

create index idx_products_is_discounts
    on products(is_discounts);

create index idx_products_is_tax
    on products(is_tax);

create index idx_discounts_product_id
    on discounts(product_id);

create index idx_discounts_product_range
    on discounts(product_id, from_number, end_number);

create index idx_banners_name
    on banners(banner_name);

create index idx_customer_addresses_customer_id
    on customer_addresses(customer_id);

create index idx_customer_addresses_location
    on customer_addresses(latitude, longitude);

create index idx_payment_terms_active_deadline
    on payment_terms(active, dead_line);

create index idx_payment_terms_kind_active
    on payment_terms(payment_kind, active);

create index idx_carts_customer_date
    on carts(customer_id, sales_date desc);

create index idx_carts_status_code
    on carts(status_code);

create index idx_carts_status_date
    on carts(status_code, sales_date desc);

create index idx_carts_created_at
    on carts(created_at);

create index idx_cart_items_cart_id
    on cart_items(cart_id);

create index idx_cart_items_product_id
    on cart_items(product_id);

create index idx_cart_items_product_code
    on cart_items(product_code);

create index idx_refresh_tokens_customer
    on refresh_tokens(customer_id);

create index idx_refresh_tokens_expires_at
    on refresh_tokens(expires_at);

create index idx_refresh_tokens_customer_active
    on refresh_tokens(customer_id, revoked_at, expires_at);

create index idx_audit_logs_actor_id
    on audit_logs(actor_customer_id);

create index idx_audit_logs_actor_code
    on audit_logs(actor_customer_code);

create index idx_audit_logs_action
    on audit_logs(action);

create index idx_audit_logs_entity
    on audit_logs(entity_type, entity_id);

create index idx_audit_logs_created_at
    on audit_logs(created_at);