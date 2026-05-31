create table if not exists audit_logs (
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

create index if not exists idx_audit_logs_actor_id
    on audit_logs(actor_customer_id);

create index if not exists idx_audit_logs_actor_code
    on audit_logs(actor_customer_code);

create index if not exists idx_audit_logs_action
    on audit_logs(action);

create index if not exists idx_audit_logs_entity
    on audit_logs(entity_type, entity_id);

create index if not exists idx_audit_logs_created_at
    on audit_logs(created_at);