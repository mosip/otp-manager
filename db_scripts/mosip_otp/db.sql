CREATE DATABASE :mosipdbname
	ENCODING = 'UTF8' 
	LC_COLLATE = 'en_US.UTF-8' 
	LC_CTYPE = 'en_US.UTF-8' 
	TABLESPACE = pg_default 
	OWNER = postgres
	TEMPLATE  = template0;

COMMENT ON DATABASE :mosipdbname IS 'OTP transactions and related data is stored in this database';

\c :mosipdbname postgres

DROP SCHEMA IF EXISTS otp CASCADE;
CREATE SCHEMA otp;
ALTER SCHEMA otp OWNER TO postgres;
ALTER DATABASE :mosipdbname SET search_path TO otp,pg_catalog,public;
