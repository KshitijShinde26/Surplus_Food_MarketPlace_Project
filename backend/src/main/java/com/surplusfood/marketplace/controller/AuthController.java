package com.surplusfood.marketplace.controller;

import com.surplusfood.marketplace.dto.AuthResponse;
import com.surplusfood.marketplace.dto.LoginRequest;
import com.surplusfood.marketplace.dto.LogoutRequest;
import com.surplusfood.marketplace.dto.MessageResponse;
import com.surplusfood.marketplace.dto.RefreshTokenRequest;
import com.surplusfood.marketplace.dto.RegisterRequest;
import com.surplusfood.marketplace.dto.UserResponse;
import com.surplusfood.marketplace.exception.ResourceNotFoundException;
import com.surplusfood.marketplace.mapper.UserMapper;
import com.surplusfood.marketplace.repository.UserRepository;
import com.surplusfood.marketplace.security.UserPrincipal;
import com.surplusfood.marketplace.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @GetMapping("/health")
    public MessageResponse health() {
        return new MessageResponse("Authentication service is running");
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return authService.refresh(request.refreshToken());
    }

    @PostMapping("/logout")
    public MessageResponse logout(@Valid @RequestBody LogoutRequest request) {
        authService.logout(request.refreshToken());
        return new MessageResponse("Logged out successfully");
    }

    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal UserPrincipal principal) {
        return userRepository.findByEmailIgnoreCase(principal.getEmail())
                .map(userMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user was not found"));
    }
}
