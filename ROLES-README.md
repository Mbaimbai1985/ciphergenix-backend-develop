# CipherGenix Security Platform - Role Management System

## Overview

The CipherGenix AI Security Platform uses a simplified three-tier role-based access control (RBAC) system designed specifically for AI security operations.

## Available Roles

### 🟢 USER (Default Role)
- **Automatically assigned** to all new user registrations
- **Description**: Basic access to AI security features
- **Permissions**:
  - Read own user profile and update personal information
  - Access threat detection dashboard (read-only)
  - View AI model security status (read-only)
  - Access basic security reports
- **Use Case**: End users, security analysts, developers

### 🟡 ADMIN
- **Description**: Team management capabilities with elevated permissions
- **Permissions**:
  - All USER permissions
  - Create, read, update, and delete team users
  - Manage threat detection configurations
  - Update AI model security settings
  - Access advanced security analytics
  - Team administration functions
- **Use Case**: Team leads, security managers, department administrators

### 🔴 SUPER_ADMIN
- **Description**: Full system access and platform administration
- **Permissions**:
  - All ADMIN permissions
  - Full system configuration access
  - Platform-wide security management
  - Create, modify, and delete administrators
  - Access to all system logs and audit trails
  - Manage security policies and configurations
  - Platform infrastructure management
- **Use Case**: System administrators, security officers, platform maintainers

## Role Assignment

### Default Assignment
- **New Registrations**: All new users are automatically assigned the `USER` role
- **Location**: Handled in the database creation function `create_user_account`
- **Database**: Role assignment occurs in the `user_roles` table

### Role Changes
- **Self-Service**: Users can update their own profile information but cannot change their role
- **Admin Assignment**: ADMIN and SUPER_ADMIN users can modify roles for other users
- **API Endpoint**: `PATCH /user/updaterole`
- **Validation**: Role changes are validated against the allowed roles enum

## Technical Implementation

### Database Schema
```sql
-- Roles table
CREATE TABLE roles (
    role_id BIGSERIAL PRIMARY KEY,
    role_uuid VARCHAR(40) NOT NULL,
    name VARCHAR(25) NOT NULL,
    authority TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- User-Role mapping
CREATE TABLE user_roles (
    user_role_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users (user_id),
    FOREIGN KEY (role_id) REFERENCES roles (role_id)
);
```

### Role Validation
- **Enum Definition**: `userservice.enumeration.Role`
- **Validation Methods**: 
  - `Role.isValid(String role)` - Check if role string is valid
  - `Role.fromString(String role)` - Convert string to Role enum
- **Error Handling**: Invalid roles throw `IllegalArgumentException`

### API Integration
```json
// Role update request
{
  "role": "ADMIN"  // Valid values: "USER", "ADMIN", "SUPER_ADMIN"
}

// Success response
{
  "status": "OK",
  "message": "User role updated successfully to ADMIN",
  "data": {
    "user": { ... }
  }
}

// Error response
{
  "status": "BAD_REQUEST",
  "message": "Invalid role: MANAGER. Valid roles are: USER, ADMIN, SUPER_ADMIN"
}
```

## Security Considerations

### Role Escalation Prevention
- Users cannot self-assign higher privilege roles
- Role changes require appropriate administrative permissions
- All role changes are logged for audit purposes

### Permission Hierarchy
```
SUPER_ADMIN
    ├── Full system access
    ├── Can manage all users and admins
    └── Platform configuration access

ADMIN
    ├── Team management
    ├── Advanced security features
    └── Cannot modify other admins or super admins

USER
    ├── Basic security features
    ├── Personal profile management
    └── Read-only access to most features
```

## Setup Instructions

### 1. Database Setup
Run the role setup script to initialize the three core roles:
```bash
psql -h localhost -U postgres -d ciphergenix_auth_db -f database-schemas/02-setup-roles.sql
```

### 2. Verify Installation
Check that roles are properly created:
```sql
SELECT name, authority FROM roles ORDER BY role_id;
```

Expected output:
```
    name     |                    authority                    
-------------+------------------------------------------------
 USER        | ROLE_USER,READ:user,UPDATE:user,READ:threats...
 ADMIN       | ROLE_ADMIN,READ:user,UPDATE:user,CREATE:user...
 SUPER_ADMIN | ROLE_SUPER_ADMIN,READ:user,UPDATE:user,CREATE...
```

### 3. Test Role Assignment
Create a test user and verify they receive the USER role:
```bash
# Register a new user via API
curl -X POST http://localhost:8086/user/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Test",
    "lastName": "User", 
    "email": "test@ciphergenix.com",
    "username": "testuser",
    "password": "SecurePass123!"
  }'
```

## Troubleshooting

### Common Issues

1. **Role not found error**
   - Ensure the role setup script has been run
   - Check that all three roles exist in the database

2. **Invalid role validation**
   - Verify role names are exactly: "USER", "ADMIN", "SUPER_ADMIN"
   - Check case sensitivity (roles should be uppercase)

3. **Permission denied**
   - Ensure user has appropriate permissions to modify roles
   - Verify authentication token is valid

### Support
For role-related issues, contact the CipherGenix Security Team or check the system logs for detailed error messages.

## Version History
- **v1.0**: Initial three-role system implementation
- **Current**: Simplified role structure with default USER assignment