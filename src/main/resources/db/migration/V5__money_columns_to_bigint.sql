alter table products
alter column price type bigint using price::bigint;

alter table carts
alter column subtotal type bigint using subtotal::bigint,
    alter column discount_total type bigint using discount_total::bigint,
    alter column tax_total type bigint using tax_total::bigint,
    alter column total type bigint using total::bigint;

alter table cart_items
alter column price type bigint using price::bigint,
    alter column discount type bigint using discount::bigint,
    alter column tax type bigint using tax::bigint,
    alter column total type bigint using total::bigint;