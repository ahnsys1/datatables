//ALTER ACCOUNT QJ04263 SET EDITION = 'ENTERPRISE';

// FILE FORMATS
use role accountadmin;
use warehouse compute_wh;
USE SCHEMA MYDB.MYSCHEMA;

// Create an Employee table
CREATE OR REPLACE TABLE STUDENT (
  id INTEGER,
  name VARCHAR(50),
  age INTEGER,
  marks INTEGER
);



// Create a named stage
CREATE OR REPLACE STAGE STUDENT_STAGE;

// Access the names internal stage
LIST @STUDENT_STAGE;

// Load data to customer table without file format
COPY INTO STUDENT
FROM @STUDENT_STAGE
FILE_FORMAT = (TYPE = 'CSV' SKIP_HEADER = 1);

// Select data from the table
SELECT * FROM STUDENT;

// Truncate the table
TRUNCATE TABLE STUDENT;

// Create a CSV File format
CREATE OR REPLACE FILE FORMAT CSV_FORMAT
  TYPE = 'CSV'
  FIELD_DELIMITER = ','
  RECORD_DELIMITER = '\n'
  SKIP_HEADER = 1;

// Load data to customer table with file format
COPY INTO STUDENT
FROM @STUDENT_STAGE
FILE_FORMAT = (FORMAT_NAME = CSV_FORMAT);

// Select data from the table
SELECT * FROM STUDENT;

// Create a JSON File format
CREATE OR REPLACE FILE FORMAT MY_JSON_FORMAT
  TYPE = 'JSON';


show file formats;


show accounts;
show users;

use role accountadmin;
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




// END OF DB creation



CREATE OR REPLACE STORAGE INTEGRATION s3_snowpipe_int
  TYPE = EXTERNAL_STAGE
  STORAGE_PROVIDER = S3
  ENABLED = TRUE
  STORAGE_AWS_ROLE_ARN = 'arn:aws:iam::817137372834:role/snowpipe_role'
  STORAGE_ALLOWED_LOCATIONS = ('s3://my-snowflake-bucket111/event/');
  










  //Set the Roles, Warehouses and Databases

USE ROLE ACCOUNTADMIN;
USE WAREHOUSE COMPUTE_WH;
use database mydb;
USE SCHEMA MYDB.MYSCHEMA;



//Create an Employee table
CREATE OR REPLACE TABLE EVENT (
    EVENT VARIANT
);

//Create an storage integration with s3 and iam role
CREATE OR REPLACE STORAGE INTEGRATION s3_snowpipe_int
  TYPE = EXTERNAL_STAGE
  STORAGE_PROVIDER = S3
  STORAGE_AWS_ROLE_ARN = 'arn:aws:iam::817137372834:role/snowpipe_role'
  STORAGE_ALLOWED_LOCATIONS = ('s3://my-snowflake-bucket111/')
  ENABLED = TRUE;


DESC INTEGRATION s3_snowpipe_int;


//Create a file format
CREATE OR REPLACE FILE FORMAT my_json_format
TYPE = 'JSON';

//Create a external s3 stage
CREATE OR REPLACE STAGE my_s3_snowpipe_stage
STORAGE_INTEGRATION = s3_snowpipe_int
URL = 's3://my-snowflake-bucket111/event/'
FILE_FORMAT = my_json_format;



DESC my_s3_snowpipe_stage;
LIST @my_s3_snowpipe_stage;
DESCRIBE STAGE my_s3_snowpipe_stage;



DESC INTEGRATION s3_snowpipe_int;

DESCRIBE STORAGE INTEGRATION s3_snowpipe_int;

DESC STORAGE INTEGRATION s3_snowpipe_int;
//###################

//Access the external stage
list @my_s3_snowpipe_stage;
SHOW STAGES;
DESC STORAGE INTEGRATION s3_snowpipe_int;
SELECT CURRENT_ROLE();


SHOW PIPES LIKE 'S3_PIPE';

  //ALTER PIPE s3_pipe REFRESH;

//Create a snowpipe to load the 


create or replace pipe s3_pipe
auto_ingest = true AS
copy into event
from @my_s3_snowpipe_stage
FILE_FORMAT = (FORMAT_NAME = my_json_format);

//Select the status of the pipe
SELECT SYSTEM$PIPE_STATUS('s3_pipe');

//Get the notification channel
SHOW PIPES;

//Select data from the table
SELECT * FROM event;
truncate event;





ALTER PIPE s3_pipe REFRESH;

SELECT * FROM event;

SELECT SYSTEM$PIPE_STATUS('s3_pipe');

LIST @my_s3_snowpipe_stage;

SELECT *
FROM TABLE(
  INFORMATION_SCHEMA.COPY_HISTORY(
    TABLE_NAME => 'EVENT',
    START_TIME => DATEADD('hour', -6, CURRENT_TIMESTAMP())
  )
);


SELECT *
FROM TABLE(
  INFORMATION_SCHEMA.COPY_HISTORY(
    TABLE_NAME => 'EVENT',
    START_TIME => DATEADD('hour', -4, CURRENT_TIMESTAMP())
  )
);


SHOW PIPES LIKE 'S3_PIPE';
SELECT SYSTEM$PIPE_STATUS('s3_pipe');
ALTER PIPE s3_pipe REFRESH;
SELECT * FROM event;



