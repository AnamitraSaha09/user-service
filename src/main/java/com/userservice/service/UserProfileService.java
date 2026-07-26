package com.userservice.service;

import com.userservice.domain.UserProfile;
import com.userservice.dto.UserProfileResponse;
import com.userservice.dto.UserProfileUpdateRequest;
import com.userservice.mapper.UserProfileMapper;
import com.userservice.repository.UserProfileRepository;
import com.userservice.security.AuthenticatedUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UserProfileService {

    private final UserProfileRepository repository;

    public UserProfileService(UserProfileRepository repository) {
        this.repository = repository;
    }

    // Lazy provisioning: the profile is created on first access if absent.
    @Transactional
    public UserProfileResponse getOrCreateCurrent(AuthenticatedUser user) {
        UserProfile profile = repository.findById(user.sub())
                .orElseGet(() -> repository.save(
                        new UserProfile(user.sub(), user.email(), user.displayName())));
        return UserProfileMapper.toResponse(profile);
    }

    @Transactional
    public UserProfileResponse updateCurrent(AuthenticatedUser user, UserProfileUpdateRequest req) {
        UserProfile profile = repository.findById(user.sub())
                .orElseGet(() -> repository.save(
                        new UserProfile(user.sub(), user.email(), user.displayName())));
        UserProfileMapper.applyUpdate(profile, req);
        return UserProfileMapper.toResponse(repository.save(profile));
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getById(UUID id) {
        return repository.findById(id)
                .map(UserProfileMapper::toResponse)
                .orElseThrow(() -> new ProfileNotFoundException(id));
    }
}
