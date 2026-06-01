alter table payment_terms
    add column if not exists payment_kind varchar(32);

update payment_terms
set payment_kind = case
                       when dead_line <= 0 then 'IMMEDIATE'
                       when name ilike '%cash%' then 'IMMEDIATE'
                       when name like '%نقد%' then 'IMMEDIATE'

                       when name ilike '%cheque%' then 'CHEQUE'
                       when name ilike '%check%' then 'CHEQUE'
                       when name like '%چک%' then 'CHEQUE'

                       else 'RECEIPT'
    end
where payment_kind is null
   or payment_kind not in ('IMMEDIATE', 'RECEIPT', 'CHEQUE');

alter table payment_terms
    alter column payment_kind set not null;

do $$
begin
    if not exists (
        select 1 from pg_constraint where conname = 'ck_payment_terms_kind'
    ) then
alter table payment_terms
    add constraint ck_payment_terms_kind
        check (payment_kind in ('IMMEDIATE', 'RECEIPT', 'CHEQUE'));
end if;
end $$;

create index if not exists idx_payment_terms_kind_active
    on payment_terms(payment_kind, active);