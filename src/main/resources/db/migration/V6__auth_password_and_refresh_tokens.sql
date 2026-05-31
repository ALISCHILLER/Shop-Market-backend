alter table customers
    add column if not exists password_change_required boolean not null default false;

create table if not exists refresh_tokens (
                                              id uuid primary key,
                                              customer_id uuid not null references customers(id),
    token_hash varchar(255) not null,
    expires_at timestamp with time zone not null,
    revoked_at timestamp with time zone,
                             ip_address varchar(64),
    user_agent text,
    created_at timestamp with time zone not null default now(),
    updated_at timestamp with time zone,
                             version bigint not null default 0
                             );

create index if not exists idx_refresh_tokens_customer
    on refresh_tokens(customer_id);

create index if not exists idx_refresh_tokens_token_hash
    on refresh_tokens(token_hash);

create index if not exists idx_refresh_tokens_expires_at
    on refresh_tokens(expires_at);