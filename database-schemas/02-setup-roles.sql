-- =====================================================================
-- CipherGenix Platform - Role Setup Script
-- =====================================================================
-- This script sets up the three core roles for the CipherGenix platform
-- Version: 1.0
-- Author: CipherGenix Development Team
-- =====================================================================

-- Connect to the user database
\c ciphergenix_auth_db;

-- Enable UUID extension if not already enabled
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Clear existing roles (be careful in production)
DELETE FROM user_roles WHERE role_id IN (SELECT role_id FROM roles);
DELETE FROM roles;

-- Reset the role_id sequence
ALTER SEQUENCE roles_role_id_seq RESTART WITH 1;

-- Insert the three core CipherGenix roles
-- NOTE: USER role is the default role assigned to all new registrations
INSERT INTO roles (role_uuid, name, authority) VALUES 
(
    uuid_generate_v4()::varchar(40), 
    'USER', 
    'ROLE_USER,READ:user,UPDATE:user,READ:threats,READ:models,READ:dashboard'
),
(
    uuid_generate_v4()::varchar(40), 
    'ADMIN', 
    'ROLE_ADMIN,READ:user,UPDATE:user,CREATE:user,DELETE:user,READ:admin,UPDATE:admin,READ:threats,UPDATE:threats,READ:models,UPDATE:models,READ:dashboard,UPDATE:dashboard,MANAGE:team'
),
(
    uuid_generate_v4()::varchar(40), 
    'SUPER_ADMIN', 
    'ROLE_SUPER_ADMIN,READ:user,UPDATE:user,CREATE:user,DELETE:user,READ:admin,UPDATE:admin,CREATE:admin,DELETE:admin,READ:super_admin,UPDATE:super_admin,CREATE:super_admin,DELETE:super_admin,READ:threats,UPDATE:threats,CREATE:threats,DELETE:threats,READ:models,UPDATE:models,CREATE:models,DELETE:models,READ:dashboard,UPDATE:dashboard,CREATE:dashboard,DELETE:dashboard,MANAGE:system,MANAGE:security,MANAGE:platform'
);

-- Verify the roles were created
SELECT 
    r.role_id,
    r.role_uuid,
    r.name,
    r.authority,
    r.created_at,
    r.updated_at
FROM roles r
ORDER BY r.role_id;

-- Display role summary
SELECT 
    'CipherGenix Role Setup Complete' as status,
    COUNT(*) as total_roles,
    STRING_AGG(name, ', ' ORDER BY role_id) as available_roles
FROM roles;

-- Create a function to get role ID by name (helper for other scripts)
CREATE OR REPLACE FUNCTION get_role_id_by_name(role_name VARCHAR(25))
RETURNS BIGINT
LANGUAGE plpgsql
AS $$
DECLARE
    role_id_result BIGINT;
BEGIN
    SELECT role_id INTO role_id_result 
    FROM roles 
    WHERE name = role_name;
    
    IF role_id_result IS NULL THEN
        RAISE EXCEPTION 'Role % not found', role_name;
    END IF;
    
    RETURN role_id_result;
END;
$$;

-- Grant appropriate permissions
GRANT SELECT ON roles TO ciphergenix_user;
GRANT USAGE ON SEQUENCE roles_role_id_seq TO ciphergenix_user;

COMMIT;