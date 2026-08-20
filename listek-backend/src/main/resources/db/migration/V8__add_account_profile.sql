alter table bank_account add column email varchar(160) not null default 'jan.kral@example.com';
alter table bank_account add column address varchar(240) not null default 'Dlouhá 12, 110 00 Praha 1';
alter table bank_account add column password_hash varchar(200) not null default '';

update bank_account
set email = 'jan.kral@example.com', address = 'Dlouhá 12, 110 00 Praha 1'
where account_number = '123456789 / 3030';

update bank_account
set email = 'jan.kral.sporeni@example.com', address = 'Dlouhá 12, 110 00 Praha 1'
where account_number = '987654321 / 3030';