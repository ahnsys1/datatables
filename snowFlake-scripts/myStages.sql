USE ROLE ACCOUNTADMIN;
USE WAREHOUSE COMPUTE_WH;
USE SCHEMA MYDB.MYSCHEMA;
//use database mydb;

// INTERNAL STAGE

 //Create an Employee table
CREATE OR REPLACE TABLE customer (
    id INTEGER,
    name VARCHAR(50),
    age INTEGER,
    state VARCHAR(50)
);

//Access the table stage
list @%customer;

//Access the user stage
list @~;

//Create a named stage
CREATE OR REPLACE STAGE CUSTOMER_STAGE;

//Access the names internal stage
list @CUSTOMER_STAGE;

//Truncate the table
TRUNCATE TABLE CUSTOMER;

//undrop schema myschema;

//Load data to customer table
copy into customer
from @CUSTOMER_STAGE
file_format = (TYPE = 'CSV' SKIP_HEADER = 1);


//Select data from the table
SELECT * FROM CUSTOMER;

//undrop schema myschema;













// EXTERNAL STAGE
//Create an Employee table
CREATE OR REPLACE TABLE USER (
id INTEGER,
name VARCHAR(50),
location VARCHAR(50),
email VARCHAR(50)
);

PRI PREGENEROVANI JE NUTNE AKTUALIZOVAT Trusted entities V trust relationships
//Create an storage integration with s3 and iam role
CREATE OR REPLACE STORAGE INTEGRATION s3_int
TYPE = EXTERNAL_STAGE
STORAGE_PROVIDER = 'S3'
ENABLED = TRUE
STORAGE_AWS_ROLE_ARN = 'arn:aws:iam::817137372834:role/snowflakerole'
STORAGE_ALLOWED_LOCATIONS = ('s3://my-snowflake-bucket111/event/');


//Describe the storage integration
DESC INTEGRATION s3_int;

//Create a file format
CREATE OR REPLACE FILE FORMAT my_csv_format
TYPE = 'CSV'
FIELD_DELIMITER = ','
RECORD_DELIMITER = '\n'
SKIP_HEADER = 1;



CREATE OR REPLACE FILE FORMAT my_json_format
TYPE = 'JSON';

CREATE OR REPLACE STAGE my_s3_stage
STORAGE_INTEGRATION = s3_int
URL = 's3://my-snowflake-bucket111/event/'
FILE_FORMAT = my_json_format;


//Access the external stage
list @my_s3_stage;

COPY INTO EVENT
from @my_s3_stage
FILE_FORMAT = (FORMAT_NAME = my_JSON_format);





select * from event;

/////////////////////////////// UP IS WORKIG

// PRIRADIT ROLI UZIVATELI
//Create an storage integration with s3 and iam role
CREATE OR REPLACE STORAGE INTEGRATION s3_int2
TYPE = EXTERNAL_STAGE
STORAGE_PROVIDER = 'S3'
ENABLED = TRUE
STORAGE_AWS_ROLE_ARN = 'arn:aws:iam::817137372834:role/snowflakerole2'
STORAGE_ALLOWED_LOCATIONS = ('s3://my-snowflake-bucket111/users/');

desc integration s3_int2;

CREATE OR REPLACE STAGE my_s3_stage2
STORAGE_INTEGRATION = s3_int2
URL = 's3://my-snowflake-bucket111/users/'
FILE_FORMAT = my_json_format;

list @my_s3_stage2;


COPY INTO user
from @my_s3_stage2
FILE_FORMAT = (FORMAT_NAME = my_csv_format);

select * from user;









// CONTINUOS INGESTION


//Create an Employee table
CREATE OR REPLACE TABLE EVENT2 (
  EVENT VARIANT
);

//Create an storage integration with s3 and iam role
CREATE OR REPLACE STORAGE INTEGRATION s3_snowpipe_int
  TYPE = EXTERNAL_STAGE
  STORAGE_PROVIDER = 'S3'
  ENABLED = TRUE
  STORAGE_AWS_ROLE_ARN = 'arn:aws:iam::817137372834:role/snowpipe_role'
  STORAGE_ALLOWED_LOCATIONS = ('s3://my-snowflake-bucket111/event2/');

//Describe the storage integration
DESC INTEGRATION s3_snowpipe_int;

//Create a file format
CREATE OR REPLACE FILE FORMAT my_json_format
  TYPE = 'JSON';

//Create a external s3 stage
CREATE OR REPLACE STAGE my_s3_snowpipe_stage
  STORAGE_INTEGRATION = s3_snowpipe_int
  URL = 's3://my-snowflake-bucket111/event2/'
  FILE_FORMAT = my_json_format;

//Access the external stage
list @my_s3_snowpipe_stage;

//Create a snowpipe to load the event data from s3
create or replace pipe s3_pipe
auto_ingest = true AS
copy into event2
from @my_s3_snowpipe_stage
FILE_FORMAT = (FORMAT_NAME = my_json_format);



//Select the status of the pipe
SELECT SYSTEM$PIPE_STATUS('s3_pipe');


show pipes;

select * from event2;



