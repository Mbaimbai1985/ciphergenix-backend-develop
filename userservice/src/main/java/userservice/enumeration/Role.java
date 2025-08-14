package userservice.enumeration;

/**
 * CipherGenix Security Platform Role Enumeration
 * 
 * Defines the three core roles for the AI security platform:
 * - USER: Default role assigned to all new registrations with basic access to security features
 * - ADMIN: Administrator with elevated permissions for team management
 * - SUPER_ADMIN: Super administrator with full system access
 */
public enum Role {
    USER("USER", "Default role for new users with basic AI security features access"),
    ADMIN("ADMIN", "Administrator with team management capabilities"),
    SUPER_ADMIN("SUPER_ADMIN", "Super administrator with full system access");
    
    private final String name;
    private final String description;
    
    Role(String name, String description) {
        this.name = name;
        this.description = description;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Check if a role string is valid
     */
    public static boolean isValid(String role) {
        if (role == null || role.trim().isEmpty()) {
            return false;
        }
        
        for (Role r : Role.values()) {
            if (r.getName().equalsIgnoreCase(role.trim())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Get role by name (case insensitive)
     */
    public static Role fromString(String role) {
        if (role == null || role.trim().isEmpty()) {
            throw new IllegalArgumentException("Role cannot be null or empty");
        }
        
        for (Role r : Role.values()) {
            if (r.getName().equalsIgnoreCase(role.trim())) {
                return r;
            }
        }
        throw new IllegalArgumentException("Invalid role: " + role + ". Valid roles are: USER, ADMIN, SUPER_ADMIN");
    }
}