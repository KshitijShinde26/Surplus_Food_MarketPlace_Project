package com.surplusfood.marketplace.mapper;

import com.surplusfood.marketplace.dto.UserResponse;
import com.surplusfood.marketplace.entity.Role;
import com.surplusfood.marketplace.entity.User;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        Set<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .map(Enum::name)
                .collect(Collectors.toUnmodifiableSet());

        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                user.getAccountStatus(),
                user.isEmailVerified(),
                user.getLatitude(),
                user.getLongitude(),
                roles
        );
    }
}
