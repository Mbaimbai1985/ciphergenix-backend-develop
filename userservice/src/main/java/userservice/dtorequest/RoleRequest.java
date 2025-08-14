package userservice.dtorequest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import userservice.enumeration.Role;

/**
 * Role Request DTO for CipherGenix Security Platform
 * 
 * Validates that only valid roles (USER, ADMIN, SUPER_ADMIN) are accepted
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class RoleRequest {
    
    @NotEmpty(message = "Role cannot be empty or null")
    private String role;
    
    /**
     * Validate the role using the Role enum
     */
    public void setRole(String role) {
        if (role != null && !role.trim().isEmpty()) {
            // Validate that the role is one of the allowed values
            if (!Role.isValid(role)) {
                throw new IllegalArgumentException(
                    "Invalid role: " + role + ". Valid roles are: USER, ADMIN, SUPER_ADMIN"
                );
            }
            this.role = role.trim().toUpperCase();
        } else {
            this.role = role;
        }
    }
    
    /**
     * Get the Role enum value
     */
    public Role getRoleEnum() {
        return Role.fromString(this.role);
    }
}