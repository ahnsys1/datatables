
//ALTER ACCOUNT QJ04263 SET EDITION = 'ENTERPRISE';

//show accounts;
drop data mydb;

use role accountadmin;
use role orgadmin;
use warehouse compute_wh;

create or replace database mydb;

create or replace schema MYDB.myschema;


create or replace table PERMANENT_TABLE 
(
    id int,
    name string
);

alter table PERMANENT_TABLE SET DATA_RETENTION_TIME_IN_DAYS = 1;

create or replace transient table TRASIENT_TABLE 
(
    id int,
    name string
);
alter table TRASIENT_TABLE SET DATA_RETENTION_TIME_IN_DAYS = 1;

create or replace temporary table TEMPORARY_TABLE 
(
    id int,
    name string
);
alter table TEMPORARY_TABLE SET DATA_RETENTION_TIME_IN_DAYS = 1;
show tables;

create or replace TABLE employees (
    id integer,
    name varchar(50),
    department varchar(50),
    salary integer
);

insert into employees (id, name, department, salary) 
values (1, 'Pat Fay', 'HR', 50000),
        (2, 'Donald OConnel', 'IT', 75000),
        (3, 'Steven King', 'Sales', 60000),
        (4, 'Susan Mavris', 'IT', 80000),
        (5, 'Jenifer Whalen', 'Marketing', 55000);


select * from employees;


create or replace VIEW it_imployees AS
SELECT id, name, salary
FROM employees
WHERE department = 'IT';

select * from it_imployees;

create or replace secure VIEW hr_imployees AS
SELECT id, name, salary
FROM employees
WHERE department = 'HR';

select * from hr_imployees;

Create or replace VIEW employee_sallaries
AS SELECT
department, 
SUM(salary) AS total_salary
FROM employees
GROUP BY department;

select * from employee_sallaries;


create or replace MATERIALIZED VIEW materialized_eployee_salariries
AS SELECT
department, 
SUM(salary) AS total_salary
FROM employees 
GROUP BY department;

select * from materialized_eployee_salariries;

show views;





create or replace table customer (
    id integer,
    name varchar(50),
    age integer,
    state varchar(50)


);
USE ROLE ORGADMIN;
-- First disable (starts a grace period)
ALTER ACCOUNT QJ04263 DISABLE;
-- Then drop (may require waiting for the grace period)
ALTER ACCOUNT QJ04263 DROP;

USE ROLE ORGADMIN;
GRANT ROLE ORGADMIN TO USER HANYSEK123;


USE ROLE ORGADMIN;
GRANT ROLE ORGADMIN TO USER HANYSEK123;

CREATE ACCOUNT temp_drop_helper
  ADMIN_NAME = 'ADMIN'
  ADMIN_PASSWORD = 'TempDr0p!Acct2026#Xz'
  EMAIL = 'janvitasek@yahoo.com'
  EDITION = 'ENTERPRISE'
  REGION = 'AWS_EU_CENTRAL_1';

DROP ACCOUNT QJ04263 GRACE_PERIOD_IN_DAYS = 3;


USE ROLE ORGADMIN;

SHOW ACCOUNTS;

SELECT * from ACCOUNTS;
list @%customer;

list @~;

Create or replace STAGE CUSTOMER_STAGE;


list @Customer_stage;


Truncate table customer;


copy into customer
FROM @customer_stage
file_format = (TYPE= 'csv', skip_header = 1);

select * from customer;


//undrop schema myschema;




