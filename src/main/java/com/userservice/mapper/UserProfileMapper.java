package com.userservice.mapper;

import com.userservice.domain.UserProfile;
import com.userservice.dto.UserProfileResponse;
import com.userservice.dto.UserProfileUpdateRequest;

public final class UserProfileMapper {

    private UserProfileMapper() {
    }

    public static UserProfileResponse toResponse(UserProfile p) {
        return new UserProfileResponse(
                p.getId(),
                p.getEmail(),
                p.getDisplayName(),
                p.getTimezone(),
                p.isNotifyEmail(),
                p.isNotifySms(),
                p.getPhone(),
                p.getCreatedAt(),
                p.getUpdatedAt()
        );
    }

    // Null fields are left unchanged so this supports partial updates.
    public static void applyUpdate(UserProfile p, UserProfileUpdateRequest req) {
        if (req.displayName() != null) {
            p.setDisplayName(req.displayName());
        }
        if (req.timezone() != null) {
            p.setTimezone(req.timezone());
        }
        if (req.notifyEmail() != null) {
            p.setNotifyEmail(req.notifyEmail());
        }
        if (req.notifySms() != null) {
            p.setNotifySms(req.notifySms());
        }
        if (req.phone() != null) {
            p.setPhone(req.phone());
        }
    }
}
