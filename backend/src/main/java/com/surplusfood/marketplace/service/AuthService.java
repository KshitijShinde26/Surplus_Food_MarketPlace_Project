package com.surplusfood.marketplace.service;

import com.surplusfood.marketplace.dto.AuthResponse;
import com.surplusfood.marketplace.dto.LoginRequest;
import com.surplusfood.marketplace.dto.RegisterRequest;
import com.surplusfood.marketplace.entity.AccountStatus;
import com.surplusfood.marketplace.entity.RefreshToken;
import com.surplusfood.marketplace.entity.Role;
import com.surplusfood.marketplace.entity.RoleName;
import com.surplusfood.marketplace.entity.User;
import com.surplusfood.marketplace.exception.ApiException;
import com.surplusfood.marketplace.exception.ConflictException;
import com.surplusfood.marketplace.mapper.UserMapper;
import com.surplusfood.marketplace.repository.RoleRepository;
import com.surplusfood.marketplace.repository.UserRepository;
import com.surplusfood.marketplace.security.JwtService;
import com.surplusfood.marketplace.security.UserPrincipal;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final UserMapper userMapper;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        RoleName requestedRole = request.role();
        if (requestedRole == RoleName.ROLE_ADMIN) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Admin accounts cannot be self-registered");
        }

        String normalizedEmail = normalizeEmail(request.email());
        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new ConflictException("Email is already registered");
        }

        Role role = roleRepository.findByName(requestedRole)
                .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Required role is missing"));

        User user = new User();
        user.setFullName(request.fullName().trim());
        user.setEmail(normalizedEmail);
        user.setPhone(request.phone());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setAccountStatus(resolveInitialStatus(requestedRole));
        user.setLatitude(request.latitude());
        user.setLongitude(request.longitude());
        user.setRoles(new HashSet<>(Set.of(role)));

        User savedUser = userRepository.save(user);
        return createAuthResponse(savedUser);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = normalizeEmail(request.email());
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(normalizedEmail, request.password())
        );

        User user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));

        return createAuthResponse(user);
    }

    @Transactional
    public AuthResponse refresh(String refreshTokenValue) {
        RefreshToken refreshToken = refreshTokenService.validate(refreshTokenValue);
        return createAuthResponse(refreshToken.getUser());
    }

    @Transactional
    public void logout(String refreshTokenValue) {
        refreshTokenService.revoke(refreshTokenValue);
    }

    private AuthResponse createAuthResponse(User user) {
        UserPrincipal principal = UserPrincipal.from(user);
        String accessToken = jwtService.generateAccessToken(principal);
        RefreshToken refreshToken = refreshTokenService.create(user);

        return new AuthResponse(
                "Bearer",
                accessToken,
                refreshToken.getToken(),
                jwtService.getAccessTokenExpirationSeconds(),
                userMapper.toResponse(user)
        );
    }

    private AccountStatus resolveInitialStatus(RoleName role) {
        if (role == RoleName.ROLE_BUSINESS_OWNER || role == RoleName.ROLE_NGO) {
            return AccountStatus.PENDING_VERIFICATION;
        }
        return AccountStatus.ACTIVE;
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
