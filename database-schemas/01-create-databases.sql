-- =====================================================================
-- CipherGenix Platform - Database Creation Script
-- =====================================================================
-- This script creates all databases required for the CipherGenix platform
-- Version: 1.0
-- Author: CipherGenix Development Team
-- =====================================================================

-- Create application user if it doesn't exist
DO
$do$
BEGIN
   IF NOT EXISTS (
      SELECT FROM pg_catalog.pg_roles 
      WHERE rolname = 'ciphergenix_user') THEN
      
      CREATE ROLE ciphergenix_user LOGIN PASSWORD 'ciphergenix_secure_password_2024';
   END IF;
END
$do$;

-- Create databases for CipherGenix microservices
CREATE DATABASE IF NOT EXISTS ciphergenix_gateway_db
    WITH OWNER = ciphergenix_user
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.utf8'
    LC_CTYPE = 'en_US.utf8'
    TEMPLATE = template0;

CREATE DATABASE IF NOT EXISTS ciphergenix_auth_db
    WITH OWNER = ciphergenix_user
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.utf8'
    LC_CTYPE = 'en_US.utf8'
    TEMPLATE = template0;

CREATE DATABASE IF NOT EXISTS ciphergenix_vulnerability_db
    WITH OWNER = ciphergenix_user
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.utf8'
    LC_CTYPE = 'en_US.utf8'
    TEMPLATE = template0;

CREATE DATABASE IF NOT EXISTS ciphergenix_model_integrity_db
    WITH OWNER = ciphergenix_user
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.utf8'
    LC_CTYPE = 'en_US.utf8'
    TEMPLATE = template0;

CREATE DATABASE IF NOT EXISTS ciphergenix_security_engine_db
    WITH OWNER = ciphergenix_user
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.utf8'
    LC_CTYPE = 'en_US.utf8'
    TEMPLATE = template0;

CREATE DATABASE IF NOT EXISTS ciphergenix_payment_db
    WITH OWNER = ciphergenix_user
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.utf8'
    LC_CTYPE = 'en_US.utf8'
    TEMPLATE = template0;

CREATE DATABASE IF NOT EXISTS ciphergenix_ml_model_db
    WITH OWNER = ciphergenix_user
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.utf8'
    LC_CTYPE = 'en_US.utf8'
    TEMPLATE = template0;

CREATE DATABASE IF NOT EXISTS ciphergenix_notification_db
    WITH OWNER = ciphergenix_user
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.utf8'
    LC_CTYPE = 'en_US.utf8'
    TEMPLATE = template0;

-- Grant all privileges to the application user
GRANT ALL PRIVILEGES ON DATABASE ciphergenix_gateway_db TO ciphergenix_user;
GRANT ALL PRIVILEGES ON DATABASE ciphergenix_auth_db TO ciphergenix_user;
GRANT ALL PRIVILEGES ON DATABASE ciphergenix_vulnerability_db TO ciphergenix_user;
GRANT ALL PRIVILEGES ON DATABASE ciphergenix_model_integrity_db TO ciphergenix_user;
GRANT ALL PRIVILEGES ON DATABASE ciphergenix_security_engine_db TO ciphergenix_user;
GRANT ALL PRIVILEGES ON DATABASE ciphergenix_payment_db TO ciphergenix_user;
GRANT ALL PRIVILEGES ON DATABASE ciphergenix_ml_model_db TO ciphergenix_user;
GRANT ALL PRIVILEGES ON DATABASE ciphergenix_notification_db TO ciphergenix_user;

-- Create extensions for each database
\c ciphergenix_gateway_db;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

\c ciphergenix_auth_db;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

\c ciphergenix_vulnerability_db;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";
CREATE EXTENSION IF NOT EXISTS "hstore";

\c ciphergenix_model_integrity_db;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";
CREATE EXTENSION IF NOT EXISTS "hstore";

\c ciphergenix_security_engine_db;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";
CREATE EXTENSION IF NOT EXISTS "hstore";

\c ciphergenix_payment_db;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

\c ciphergenix_ml_model_db;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";
CREATE EXTENSION IF NOT EXISTS "hstore";

\c ciphergenix_notification_db;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Display success message
SELECT 'CipherGenix databases created successfully!' as status;