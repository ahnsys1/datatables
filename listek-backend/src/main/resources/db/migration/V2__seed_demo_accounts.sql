insert into bank_account (id, owner_name, account_number, balance, currency)
values
    ('00000000-0000-0000-0000-000000000001', 'Jan Kral', '123456789 / 3030', 126840.35, 'CZK'),
    ('00000000-0000-0000-0000-000000000002', 'Jan Kral', '987654321 / 3030', 84200.00, 'CZK');

insert into bank_transaction (id, account_id, amount, type, description, created_at)
values
    ('10000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000001', 48500.00, 'CREDIT', 'Vyplata', current_timestamp - interval '1 day'),
    ('10000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000001', -1248.90, 'DEBIT', 'Albert', current_timestamp - interval '2 days'),
    ('10000000-0000-0000-0000-000000000003', '00000000-0000-0000-0000-000000000001', -169.00, 'DEBIT', 'Spotify', current_timestamp - interval '3 days'),
    ('10000000-0000-0000-0000-000000000004', '00000000-0000-0000-0000-000000000002', 84200.00, 'CREDIT', 'Pocatecni vklad', current_timestamp - interval '30 days');
