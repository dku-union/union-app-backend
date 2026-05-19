package com.union.union.domain.user.controller;

import com.union.union.domain.user.entity.User;
import com.union.union.domain.user.service.UserService;
import com.union.union.global.security.jwt.JwtUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMe(@AuthenticationPrincipal JwtUserPrincipal principal) {
        User user = userService.getUser(principal.userId());
        return ResponseEntity.ok(UserResponse.from(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable UUID id) {
        User user = userService.getUser(id);
        return ResponseEntity.ok(UserResponse.from(user));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers().stream()
                .map(UserResponse::from)
                .toList();
        return ResponseEntity.ok(users);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    public record UserResponse(UUID id, String email, String nickname, String universityName, boolean isVerified) {
        public static UserResponse from(User user) {
            return new UserResponse(
                    user.getId(), user.getEmail(), user.getNickname(),
                    user.getUniversityName(), user.isVerified()
            );
        }
    }
}
