package com.userservice.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UserProfileResponse(
        UUID id,
        String email,
        String displayName,
        String timezone,
        boolean notifyEmail,
        boolean notifySms,
        String phone,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
