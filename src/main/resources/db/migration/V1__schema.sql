create sequence if not exists cart_code_seq start with 100001 increment by 1;

create table customers (
    id uuid primary key,
    customer_code varchar(64) not null unique,
    customer_name varchar(255) not null,
    mobile varchar(32),
    phone varchar(32),
    center varchar(255),
    national_code varchar(32),
    password_hash varchar(255) not null,
    salt varchar(255),
    role varchar(32) not null default 'CUSTOMER',
    enabled boolean not null default true,
    created_at timestamp with time zone not null default now()
);

create table product_categories (
    product_category_code integer primary key,
    product_category_name varchar(255),
    product_category_image text,
    product_category_image_unselect text
);

create table products (
    id uuid primary key,
    product_name varchar(255),
    product_code integer not null unique,
    full_name_kala1 varchar(255),
    unit1 varchar(64),
    unitid1 varchar(64),
    convert_factor1 integer not null default 1,
    full_name_kala2 varchar(255),
    unit2 varchar(64),
    convert_factor2 integer not null default 1,
    unitid2 varchar(64),
    product_group_code integer not null references product_categories(product_category_code),
    price bigint not null check (price >= 0),
    is_discounts boolean not null default false,
    is_tax boolean not null default true,
    product_image text
);

create table discounts (
    id uuid primary key,
    product_id uuid not null references products(id) on delete cascade,
    discount_percent integer not null check (discount_percent >= 0 and discount_percent <= 100),
    from_number integer not null check (from_number >= 1),
    end_number integer not null check (end_number >= from_number)
);

create table banners (
    id uuid primary key,
    banner_image text not null,
    banner_name varchar(255) not null
);

create table customer_addresses (
    id uuid primary key,
    customer_id uuid not null references customers(id) on delete cascade,
    center_name varchar(255) not null,
    customer_address text not null,
    customer_mobile varchar(32) not null,
    customer_phone varchar(32) not null
);

create table payment_terms (
    id uuid primary key,
    name varchar(255) not null,
    dead_line integer not null default 0,
    immediate_discount_percent integer not null default 0,
    receipt_discount_percent integer not null default 0,
    cheque_discount_percent integer not null default 0,
    active boolean not null default true
);

create table carts (
    id uuid primary key,
    cart_code integer not null unique,
    customer_id uuid not null references customers(id),
    customer_address_id uuid not null references customer_addresses(id),
    payment_term_id uuid not null references payment_terms(id),
    customer_name_snapshot varchar(255) not null,
    customer_address_snapshot text not null,
    status_name varchar(128) not null,
    status_color varchar(32) not null,
    sales_date date not null,
    subtotal bigint not null default 0,
    discount_total bigint not null default 0,
    tax_total bigint not null default 0,
    total bigint not null default 0,
    created_at timestamp with time zone not null default now()
);

create table cart_items (
    id uuid primary key,
    cart_id uuid not null references carts(id) on delete cascade,
    product_id uuid not null references products(id),
    product_code integer not null,
    product_name varchar(255) not null,
    product_image_url text,
    quantity integer not null check (quantity > 0),
    price bigint not null default 0,
    discount bigint not null default 0,
    tax bigint not null default 0,
    total bigint not null default 0
);

create index idx_products_group_code on products(product_group_code);
create index idx_discounts_product_id on discounts(product_id);
create index idx_customer_addresses_customer_id on customer_addresses(customer_id);
create index idx_carts_customer_date on carts(customer_id, sales_date desc);
create index idx_cart_items_cart_id on cart_items(cart_id);
