package com.userservice.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

// Arc A stand-in for authentication: the current user comes from dev headers.
// Step 8 replaces the body of current() with the JWT `sub` and claims.
@Component
public class CurrentUserProvider {

    private final HttpServletRequest request;

    public CurrentUserProvider(HttpServletRequest request) {
        this.request = request;
    }

    public AuthenticatedUser current() {
        String rawId = request.getHeader("X-User-Id");
        if (rawId == null || rawId.isBlank()) {
            throw new ResponseStatusException(UNAUTHORIZED, "Missing X-User-Id header");
        }
        UUID sub = parse(rawId);
        String email = header("X-User-Email", sub + "@dev.local");
        String displayName = header("X-User-Name", "Dev User");
        return new AuthenticatedUser(sub, email, displayName);
    }

    private UUID parse(String rawId) {
        try {
            return UUID.fromString(rawId);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(UNAUTHORIZED, "X-User-Id is not a valid UUID");
        }
    }

    private String header(String name, String fallback) {
        String value = request.getHeader(name);
        return (value == null || value.isBlank()) ? fallback : value;
    }
}
