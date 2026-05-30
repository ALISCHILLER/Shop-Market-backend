alter table customers
    add column if not exists updated_at timestamp with time zone;

alter table customers
    add column if not exists version bigint not null default 0;

alter table products
    add column if not exists created_at timestamp with time zone not null default now();

alter table products
    add column if not exists updated_at timestamp with time zone;

alter table products
    add column if not exists version bigint not null default 0;

alter table discounts
    add column if not exists created_at timestamp with time zone not null default now();

alter table discounts
    add column if not exists updated_at timestamp with time zone;

alter table discounts
    add column if not exists version bigint not null default 0;

alter table banners
    add column if not exists created_at timestamp with time zone not null default now();

alter table banners
    add column if not exists updated_at timestamp with time zone;

alter table banners
    add column if not exists version bigint not null default 0;

alter table customer_addresses
    add column if not exists created_at timestamp with time zone not null default now();

alter table customer_addresses
    add column if not exists updated_at timestamp with time zone;

alter table customer_addresses
    add column if not exists version bigint not null default 0;

alter table payment_terms
    add column if not exists created_at timestamp with time zone not null default now();

alter table payment_terms
    add column if not exists updated_at timestamp with time zone;

alter table payment_terms
    add column if not exists version bigint not null default 0;

alter table carts
    add column if not exists status_code varchar(32) not null default 'REGISTERED';

alter table carts
    add column if not exists updated_at timestamp with time zone;

alter table carts
    add column if not exists version bigint not null default 0;

alter table cart_items
    add column if not exists created_at timestamp with time zone not null default now();

alter table cart_items
    add column if not exists updated_at timestamp with time zone;

alter table cart_items
    add column if not exists version bigint not null default 0;

update carts
set status_code = case
                      when status_name in ('ثبت', 'ثبت‌شده', 'ثبت شده') then 'REGISTERED'
                      when status_name in ('بررسی', 'درحال بررسی', 'در حال بررسی', 'پردازش') then 'PROCESSING'
                      when status_name in ('لغو', 'لغو‌شده', 'لغو شده', 'باطل') then 'CANCELLED'
                      when status_name in ('تحویل', 'تحویل‌شده', 'تحویل شده', 'ارسال شده') then 'DELIVERED'
                      else status_code
    end;

create index if not exists idx_customers_role
    on customers(role);

create index if not exists idx_customers_enabled
    on customers(enabled);

create index if not exists idx_customers_created_at
    on customers(created_at);

create index if not exists idx_products_product_name
    on products(product_name);

create index if not exists idx_products_is_discounts
    on products(is_discounts);

create index if not exists idx_products_is_tax
    on products(is_tax);

create index if not exists idx_discounts_product_range
    on discounts(product_id, from_number, end_number);

create index if not exists idx_payment_terms_active_deadline
    on payment_terms(active, dead_line);

create index if not exists idx_carts_status_code
    on carts(status_code);

create index if not exists idx_carts_status_date
    on carts(status_code, sales_date desc);

create index if not exists idx_carts_created_at
    on carts(created_at);

create index if not exists idx_cart_items_product_id
    on cart_items(product_id);

create index if not exists idx_cart_items_product_code
    on cart_items(product_code);