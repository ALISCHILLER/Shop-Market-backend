alter table customer_addresses
    add column if not exists latitude double precision;

alter table customer_addresses
    add column if not exists longitude double precision;

alter table customer_addresses
    add column if not exists is_default boolean not null default false;

update customer_addresses ca
set is_default = true
where ca.id in (
    select distinct on (customer_id) id
from customer_addresses
where is_default = false
order by customer_id, center_name asc, id asc
    )
    and not exists (
select 1
from customer_addresses x
where x.customer_id = ca.customer_id
  and x.is_default = true
    );

create unique index if not exists ux_customer_addresses_one_default
    on customer_addresses(customer_id)
    where is_default = true;

create index if not exists idx_customer_addresses_location
    on customer_addresses(latitude, longitude);