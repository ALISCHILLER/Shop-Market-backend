do $$
begin
    if not exists (
        select 1 from pg_constraint where conname = 'ck_product_categories_code_positive'
    ) then
alter table product_categories
    add constraint ck_product_categories_code_positive
        check (product_category_code > 0);
end if;
end $$;

do $$
begin
    if not exists (
        select 1 from pg_constraint where conname = 'ck_products_positive_codes_and_factors'
    ) then
alter table products
    add constraint ck_products_positive_codes_and_factors
        check (
            product_code > 0
                and product_group_code > 0
                and convert_factor1 > 0
                and convert_factor2 > 0
                and price >= 0
            );
end if;
end $$;

do $$
begin
    if not exists (
        select 1 from pg_constraint where conname = 'ck_payment_terms_valid_values'
    ) then
alter table payment_terms
    add constraint ck_payment_terms_valid_values
        check (
            dead_line >= 0
                and immediate_discount_percent between 0 and 100
                and receipt_discount_percent between 0 and 100
                and cheque_discount_percent between 0 and 100
            );
end if;
end $$;

do $$
begin
    if not exists (
        select 1 from pg_constraint where conname = 'ck_customer_addresses_geo_pair'
    ) then
alter table customer_addresses
    add constraint ck_customer_addresses_geo_pair
        check (
            (latitude is null and longitude is null)
                or
            (
                latitude between -90 and 90
                    and longitude between -180 and 180
                )
            );
end if;
end $$;

do $$
begin
    if not exists (
        select 1 from pg_constraint where conname = 'ck_carts_money_non_negative'
    ) then
alter table carts
    add constraint ck_carts_money_non_negative
        check (
            subtotal >= 0
                and discount_total >= 0
                and tax_total >= 0
                and total >= 0
            );
end if;
end $$;

do $$
begin
    if not exists (
        select 1 from pg_constraint where conname = 'ck_cart_items_valid_values'
    ) then
alter table cart_items
    add constraint ck_cart_items_valid_values
        check (
            quantity > 0
                and quantity <= 1000
                and price >= 0
                and discount >= 0
                and tax >= 0
                and total >= 0
            );
end if;
end $$;

create index if not exists idx_refresh_tokens_customer_active
    on refresh_tokens(customer_id, revoked_at, expires_at);

create index if not exists idx_refresh_tokens_token_hash
    on refresh_tokens(token_hash);