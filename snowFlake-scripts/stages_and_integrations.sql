USE ROLE ACCOUNTADMIN;
USE WAREHOUSE COMPUTE_WH;
USE SCHEMA MYDB.MYSCHEMA;
use database mydb;

DESC STORAGE INTEGRATION s3_snowpipe_int;

SHOW STORAGE INTEGRATIONS;


CREATE OR REPLACE STORAGE INTEGRATION s3_snowpipe_int
  TYPE = EXTERNAL_STAGE
  STORAGE_PROVIDER = S3
  ENABLED = TRUE
  STORAGE_AWS_ROLE_ARN = 'arn:aws:iam::817137372834:role/snowpipe_role'
  STORAGE_ALLOWED_LOCATIONS = ('s3://my-snowflake-bucket111/event/');




//Describe the storage integration
DESC INTEGRATION s3_snowpipe_int;

//Create a file format
CREATE OR REPLACE FILE FORMAT my_json_format
  TYPE = 'JSON';

//Create a external s3 stage
CREATE OR REPLACE STAGE my_s3_snowpipe_stage
  STORAGE_INTEGRATION = s3_snowpipe_int
  URL = 's3://my-snowflake-bucket111/event/'
  FILE_FORMAT = my_json_format;

//Access the external stage
LIST @my_s3_snowpipe_stage;

//Create a snowpipe to load the event data from s3
CREATE OR REPLACE PIPE s3_pipe
  AUTO_INGEST = TRUE AS
  COPY INTO event
    FROM @my_s3_snowpipe_stage
    FILE_FORMAT = (FORMAT_NAME = my_json_format);

//Select the status of the pipe
SELECT SYSTEM$PIPE_STATUS('s3_pipe');

//Get the notification channel
SHOW PIPES;

//Select data from the table
SELECT * FROM event;

  
USE SCHEMA MYDB.MYSCHEMA;

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

//Load data to customer table
copy into customer
from @CUSTOMER_STAGE
file_format = (TYPE = 'CSV' SKIP_HEADER = 1);

