package com.ciphergenix.security.pipeline;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

@Component
public class RoleBasedAccessControl {

    public boolean hasRole(Authentication auth, String role) {
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }
        for (GrantedAuthority authority : auth.getAuthorities()) {
            if (authority.getAuthority().equals("ROLE_" + role)) {
                return true;
            }
        }
        return false;
    }
}