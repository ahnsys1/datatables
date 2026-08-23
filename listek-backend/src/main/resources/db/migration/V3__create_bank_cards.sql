create table if not exists bank_card (
    id uuid primary key,
    account_id uuid not null references bank_account(id),
    holder_name varchar(120) not null,
    card_type varchar(40) not null,
    last_four varchar(4) not null,
    expiration_date varchar(5) not null,
    payment_limit numeric(19, 2) not null check (payment_limit >= 0),
    withdrawal_limit numeric(19, 2) not null check (withdrawal_limit >= 0),
    locked boolean not null default false,
    online_payments boolean not null default true,
    cash_withdrawals boolean not null default true
);

create index if not exists idx_bank_card_account on bank_card(account_id);

do $$
begin
    if not exists (
        select 1 from pg_constraint where conname = 'bank_card_account_fk'
    ) then
        alter table bank_card
            add constraint bank_card_account_fk foreign key (account_id) references bank_account(id);
    end if;
end $$;

insert into bank_card (
    id, account_id, holder_name, card_type, last_four, expiration_date,
    payment_limit, withdrawal_limit, locked, online_payments, cash_withdrawals
)
values (
    '20000000-0000-0000-0000-000000000001',
    '00000000-0000-0000-0000-000000000001',
    'Jan Kral', 'Visa Classic', '2841', '08/29',
    50000.00, 10000.00, false, true, true
)
on conflict (id) do nothing;
