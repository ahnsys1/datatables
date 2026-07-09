// Set the Roles, Warehouses and Databases
USE ROLE ACCOUNTADMIN;
USE WAREHOUSE COMPUTE_WH;
USE SCHEMA MYDB.MYSCHEMA;
use database mydb;
 
//GRANT USAGE ON INTEGRATION s3_snowpipe_int TO ROLE ACCOUNTADMIN;
// Create an Employee table
CREATE OR REPLACE TABLE USER (
  id INTEGER,
  name VARCHAR(50),
  location VARCHAR(50),
  email VARCHAR(50)
);

//DESCRIBE ROLE snowflakerole;

    

/////////////////////////////////////////////////////

//LIST @my_stage;
// Create an storage integration with s3 and iam role
CREATE OR REPLACE STORAGE INTEGRATION S3_INT_PIPE
  TYPE = EXTERNAL_STAGE
  STORAGE_PROVIDER = 'S3'
  ENABLED = TRUE
  STORAGE_AWS_ROLE_ARN = 'arn:aws:iam::817137372834:role/snowflakerole'
  STORAGE_ALLOWED_LOCATIONS = ('s3://my-snowflake-bucket111/event');

// Describe the storage integratio
  //STORAGE_AWS_aws sts get-caller-identityROLE_ARN = 'arn:aws:iam::260289090760:user/233p1000-s'
DESC INTEGRATION s3_int;
//SELECT SYSTEM$VALIDATE_STORAGE_INTEGRATION('S3_INT');
DESC STAGE my_s3_stage;

list @my_s3_stage;





//Create a file format
CREATE OR REPLACE FILE FORMAT my_csv_format
TYPE = 'CSV'
FIELD_DELIMITER = ','
RECORD_DELIMITER = '\n'
SKIP_HEADER = 1;

SHOW ORGANIZATION ACCOUNTS;

//Create a external s3 stage
CREATE OR REPLACE STAGE my_s3_stage
STORAGE_INTEGRATION = S3_INT
URL = 's3://my-snowflake-bucket111/' 
FILE_FORMAT = my_csv_format;


//s3://my-snowflake-bucket111/
//Access the external stage

DESCRIBE INTEGRATION s3_int;
DESC STORAGE INTEGRATION s3_snowpipe_int;
describe stage my_s3_stage;



COPY INTO USER
FROM @my_s3_stage/USER_EXPORT.csv 
FILE_FORMAT = (FORMAT_NAME = my_csv_format);


//Select data from the table
SELECT * FROM USER;



