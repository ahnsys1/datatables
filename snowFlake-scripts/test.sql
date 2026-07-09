SELECT CURRENT_ACCOUNT_NAME() || '.' || CURRENT_ORGANIZATION_NAME() || '.snowflakecomputing.com' AS server_url;

PUT 'file:///home/ahanys/Desktop/DT/ACTIONS/datatables/CUSTOMER_EXPORT.csv' @MYDB.MYSCHEMA.CUSTOMER_STAGE;


list @MYDB.MYSCHEMA.CUSTOMER_STAGE;

