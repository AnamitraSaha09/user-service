package com.userservice.web;

import com.userservice.dto.UserProfileResponse;
import com.userservice.dto.UserProfileUpdateRequest;
import com.userservice.security.CurrentUserProvider;
import com.userservice.service.UserProfileService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserProfileService service;
    private final CurrentUserProvider currentUser;

    public UserController(UserProfileService service, CurrentUserProvider currentUser) {
        this.service = service;
        this.currentUser = currentUser;
    }

    @GetMapping("/me")
    public UserProfileResponse getCurrentUser() {
        return service.getCurrent(currentUser.current());
    }

    @PostMapping("/me")
    @ResponseStatus(HttpStatus.CREATED)
    public UserProfileResponse createCurrentUser() {
        return service.createCurrent(currentUser.current());
    }

    @PutMapping("/me")
    public UserProfileResponse updateMyProfile(@Valid @RequestBody UserProfileUpdateRequest req) {
        return service.updateCurrent(currentUser.current(), req);
    }

    // Authorization (caller == {userId} or admin) is added in Step 10.
    @GetMapping("/{userId}")
    public UserProfileResponse getUserById(@PathVariable UUID userId) {
        return service.getById(userId);
    }
}
