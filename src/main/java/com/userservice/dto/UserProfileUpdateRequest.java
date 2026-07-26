package com.userservice.dto;

import jakarta.validation.constraints.Size;

public record UserProfileUpdateRequest(
        @Size(max = 100) String displayName,
        @Size(max = 50) String timezone,
        Boolean notifyEmail,
        Boolean notifySms,
        @Size(max = 30) String phone
) {
}
