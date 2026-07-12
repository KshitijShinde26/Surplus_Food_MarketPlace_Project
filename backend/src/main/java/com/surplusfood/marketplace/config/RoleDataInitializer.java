package com.surplusfood.marketplace.config;

import com.surplusfood.marketplace.entity.Category;
import com.surplusfood.marketplace.entity.Role;
import com.surplusfood.marketplace.entity.RoleName;
import com.surplusfood.marketplace.repository.CategoryRepository;
import com.surplusfood.marketplace.repository.RoleRepository;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoleDataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final CategoryRepository categoryRepository;
    private final com.surplusfood.marketplace.repository.UserRepository userRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        Map<RoleName, String> descriptions = Map.of(
                RoleName.ROLE_ADMIN, "Platform administrator",
                RoleName.ROLE_BUSINESS_OWNER, "Business account that lists surplus food",
                RoleName.ROLE_CONSUMER, "Consumer account that buys discounted food",
                RoleName.ROLE_NGO, "NGO or shelter account that claims donations"
        );

        descriptions.forEach((roleName, description) -> roleRepository.findByName(roleName)
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName(roleName);
                    role.setDescription(description);
                    return roleRepository.save(role);
                }));

        if (categoryRepository.count() == 0) {
            java.util.List.of("Bakery", "Prepared Meals", "Groceries", "Produce", "Dairy", "Beverages").forEach(name -> {
                Category cat = new Category();
                cat.setName(name);
                cat.setActive(true);
                categoryRepository.save(cat);
            });
        }

        if (userRepository.findByEmailIgnoreCase("admin@surplusfood.com").isEmpty()) {
            Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
                    .orElseThrow(() -> new RuntimeException("ROLE_ADMIN missing"));
            com.surplusfood.marketplace.entity.User admin = new com.surplusfood.marketplace.entity.User();
            admin.setFullName("System Admin");
            admin.setEmail("admin@surplusfood.com");
            admin.setPhone("+1234567890");
            admin.setPasswordHash(passwordEncoder.encode("admin"));
            admin.setAccountStatus(com.surplusfood.marketplace.entity.AccountStatus.ACTIVE);
            admin.setEmailVerified(true);
            admin.setLatitude(new java.math.BigDecimal("37.7749"));
            admin.setLongitude(new java.math.BigDecimal("-122.4194"));
            admin.setRoles(new java.util.HashSet<>(java.util.Set.of(adminRole)));
            userRepository.save(admin);
        }
    }
}
