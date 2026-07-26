package com.userservice.service;

import java.util.UUID;

public class ProfileNotFoundException extends RuntimeException {
    public ProfileNotFoundException(UUID id) {
        super("User profile not found: " + id);
    }
}
