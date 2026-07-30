package com.userservice.service;

import java.util.UUID;

public class ProfileAlreadyExistsException extends RuntimeException {
    public ProfileAlreadyExistsException(UUID id) {
        super("User profile already exists: " + id);
    }
}
