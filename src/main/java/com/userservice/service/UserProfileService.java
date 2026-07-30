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

    @Transactional(readOnly = true)
    public UserProfileResponse getCurrent(AuthenticatedUser user) {
        return repository.findById(user.sub())
                .map(UserProfileMapper::toResponse)
                .orElseThrow(() -> new ProfileNotFoundException(user.sub()));
    }

    @Transactional
    public UserProfileResponse createCurrent(AuthenticatedUser user) {
        if (repository.existsById(user.sub())) {
            throw new ProfileAlreadyExistsException(user.sub());
        }
        UserProfile profile = repository.save(
                new UserProfile(user.sub(), user.email(), user.displayName()));
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
