package io.github.lyu929.ems.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/** Name of the authenticated user, for audit records ("system" outside a request). */
public final class CurrentUser {

    private CurrentUser() {}

    public static String name() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth == null || auth.getName() == null ? "system" : auth.getName();
    }
}
