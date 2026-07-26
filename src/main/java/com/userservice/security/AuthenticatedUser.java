package com.userservice.security;

import java.util.UUID;

public record AuthenticatedUser(UUID sub, String email, String displayName) {
}
